#!/usr/bin/env jruby

=begin
  Licensed under the Apache License, Version 2.0 (the "License"); 
  you may not use this file except in compliance with the License. 
  You may obtain a copy of the License at:
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software 
  distributed under the License is distributed on an "AS IS" BASIS, 
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  implied. See the License for the specific language governing 
  permissions and limitations under the License.
=end

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
  
  # META-INF/MANIFEST.MF
  manifest = jarFile.manifest
  
  # There may be a plugin.properties file containing the plugin and provider names
  propsName = !manifest.main_attributes.get_value("Fragment-Host").nil? ? "fragment" :
              !manifest.main_attributes.get_value("Eclipse-SystemBundle").nil? ? "systembundle" :
              "plugin"
  props = Java::java.util.Properties.new
  propsEntry = jarFile.get_jar_entry("#{propsName}.properties")
  if propsEntry
    is = jarFile.get_input_stream(propsEntry)
    props.load(is)
    is.close
  end
  
  artifactId = manifest.main_attributes.get_value("Bundle-SymbolicName").gsub(/;.*/, "")
  version = manifest.main_attributes.get_value("Bundle-Version")
  name = manifest.main_attributes.get_value("Bundle-Name")
  name ||= artifactId
  if name.start_with? "%"
    name = props[name[1..name.length]]
  end
  
  vendor = manifest.main_attributes.get_value("Bundle-Vendor")
  vendor ||= ""
  if vendor.start_with? "%"
    vendor = props[vendor[1..vendor.length]]
  end
  
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
  
  jarFile.close
  
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
      xml << "
  <organization>
    <name>#{CGI.escapeHTML(bundle[:vendor])}</name>
  </organization>
"
    end
  
    xml << "
  <licenses>
    <license>
      <name>International License Agreement for Non-Warranted Programs</name>
      <distribution>manual</distribution>
      <comments>Refer to the license and notice files in the project download for details</comments>
    </license>
  </licenses>
  
"

  if not bundle[:requires].empty?
    xml << "  <dependencies>\n"
    
    bundle[:requires].each do |bundleName|
      rbundle = bundles[bundleName]
      
      if not rbundle.nil?
        xml << "
    <dependency>
      <groupId>com.ibm.xsp</groupId>
      <artifactId>#{CGI.escapeHTML(rbundle[:artifactId])}</artifactId>
      <version>#{CGI.escapeHTML(rbundle[:version])}</version>
    </dependency>
"
      end
    end
    
    xml << "  </dependencies>\n"
  end

  xml << "</project>"
  
  File.write("temp.pom", xml, 0, open_args: "w")
  
  puts `mvn install:install-file #{options[:install] ? '' : '-DlocalRepositoryPath=m2repo'} -DgroupId=com.ibm.xsp -DartifactId=#{bundle[:artifactId]} -Dversion=#{bundle[:version]} -Dpackaging=jar -Dfile=#{bundle[:file]} -DpomFile=temp.pom`
end

File.delete "temp.pom"