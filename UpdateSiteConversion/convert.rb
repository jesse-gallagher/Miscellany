#!/usr/bin/env jruby

require "java"
require "cgi"
require "optparse"

options = {}
OptionParser.new do |opts|
  opts.banner = "Usage: convert.rb [options]"
  
  opts.on("-h", "--help", "Prints this help") do
    puts opts
    exit
  end
  
  opts.on("-i", "--install", "Installs the artifacts into the local Maven repository instead of a folder in the current directory") do
    options[:install] = true
  end
end.parse!

bundles = {}

# Read in all the bundles for later use
Dir["UpdateSite/plugins/*.jar"].each do |file|
  jarFile = Java::java.util.jar.JarFile.new(file)
  manifest = jarFile.manifest
  
  artifactId = manifest.main_attributes.get_value("Bundle-SymbolicName").gsub(/;.*/, "")
  version = manifest.main_attributes.get_value("Bundle-Version")
  name = manifest.main_attributes.get_value("Bundle-Name")
  name ||= artifactId
  
  vendor = manifest.main_attributes.get_value("Bundle-Vendor")
  vendor ||= ""
  
  # Figure out its dependencies based on Require-Bundle
  requires = manifest.main_attributes.get_value("Require-Bundle")
  requires ||= ""
  # Strip out extra information attached to the require-bundle entries. Yes, this is horrible.
  requires = requires.gsub(/;bundle-version="[^"]+"/, "").gsub(/;[^,]+/, "").split(",")
  requires.reject! { |entry| entry.nil? or entry.empty? }
  
  bundles[artifactId] = {
    name: name,
    vendor: vendor,
    artifactId: artifactId,
    version: version,
    file: file,
    requires: requires
  }
  
end

# Exit early if there are no bundles to convert
if bundles.empty?
  abort("Could not find any bundles to convert.")
end

# Generate POM files for each bundle and install into the Maven repository
bundles.each_value do |bundle|
  puts "======="
  puts "bundle: #{bundle}"
  
 
  
  xml = <<END
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.ibm.xsp</groupId>
  <artifactId>#{CGI.escapeHTML(bundle[:artifactId])}</artifactId>
  <version>#{CGI.escapeHTML(bundle[:version])}</version>
  
  <name>#{CGI.escapeHTML(bundle[:name])}</name>
  <url>http://www.openntf.org/main.nsf/project.xsp?r=project/IBM%20Domino%20Update%20Site%20for%20Build%20Management</url>
END

    
    if not bundle[:vendor].empty?
      xml << <<END
  <organization>
    <name>#{CGI.escapeHTML(bundle[:vendor])}</name>
  </organization>
END
    end
  
    xml << <<END
  <licenses>
    <license>
      <name>International License Agreement for Non-Warranted Programs</name>
      <distribution>manual</distribution>
      <comments>Refer to the license and notice files in the project download for details</comments>
    </license>
  </licenses>
  
END

  if not bundle[:requires].empty?
    xml += "  <dependencies>\n"
    
    bundle[:requires].each do |bundleName|
      rbundle = bundles[bundleName]
      
      if not rbundle.nil?
        xml += <<END
    <dependency>
      <groupId>com.ibm.xsp</groupId>
      <artifactId>#{CGI.escapeHTML(rbundle[:artifactId])}</artifactId>
      <version>#{CGI.escapeHTML(rbundle[:version])}</version>
    </dependency>
END
      end
    end
    
    xml += "  </dependencies>\n"
  end

  xml += "</project>"
  
  File.write("temp.pom", xml, 0, open_args: "w")
  
  puts `mvn install:install-file #{options[:install] ? '' : '-DlocalRepositoryPath=m2repo'} -DgroupId=com.ibm.xsp -DartifactId=#{bundle[:artifactId]} -Dversion=#{bundle[:version]} -Dpackaging=jar -Dfile=#{bundle[:file]} -DpomFile=temp.pom`
end

File.delete "temp.pom"