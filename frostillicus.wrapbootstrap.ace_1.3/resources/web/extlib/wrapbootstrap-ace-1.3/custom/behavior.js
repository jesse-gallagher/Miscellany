var AceBehavior = {
	".chzn-select": {
		found: function(input) {
			$(input).chosen().on("change", function() {
				input.dispatchEvent(new Event("change"));
			})
		}
	},
	".select2": function(input) {
		$(input).select2();
	},
	
	".date-picker": {
		found: function(input) {
			$(input).datepicker().next().on("click", function(){
				$(this).prev().focus();
			})
		}
	},
	".time-picker": {
		found: function(input) {
			$(input).timepicker().next().on("click", function(){
				$(this).prev().focus();
			})
		}
	},
	".datetime-picker": {
		found: function(input) {
			try {
			$(input).datetimepicker().next().on("click", function() {
				$(this).prev().focus();
			})
			} catch(e) {
				console.log(e)
			}
		}
	},
	
	"input[type='file']": function(input) {
		$(input).ace_file_input({
			no_file: "",
			btn_choose: "Choose",
			btn_change: "Change",
			onchange: null,
			droppable: false,
			thumbnail: false,
			icon_remove: false // TODO find out why the remove icon doesn't work
		})
	}
}

dojo.ready(function() {
	dojo.behavior.add(AceBehavior);
	dojo.behavior.apply();
});

// Make sure that future pagers are also straightened out
dojo.subscribe("partialrefresh-complete", null, function(method, form, refreshId) {
	dojo.behavior.apply();
});