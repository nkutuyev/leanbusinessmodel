var lbm = {};
var contentContainerElement;

lbm.RegionManager = function (options) {
			
    var currentView;
    var template = Handlebars.compile($("#content-container-template").html());
    var el = options.el;
    var region = {};

    var closeView = function (view) {
        if (view) {
            view.close();
        }
    };

    var openView = function (view) {
    	el.append(template());
    	contentContainerElement = $(".content-container");
        view.setElement(contentContainerElement);
        view.render();
    };

    region.show = function (view) {
        closeView(currentView);
        currentView = view;
        openView(currentView);
    };

    return region;
};

Backbone.View.prototype.close = function(){
  this.remove();
  this.unbind();
};

lbm.showAlert = function(message, type){
	alertPanel = $("#alert-panel");
	if(alertPanel){
		var panelClass = type ? type : "alert-danger";
		alertPanel.text(message);
		alertPanel.append(" <a id='alert-remove' href='' class='alert-info'><span class='glyphicon glyphicon-remove'></span> </a>");
		alertPanel.addClass(panelClass);
		$( "#alert-remove" ).on( "click", function(event) {
			event.stopPropagation();
			event.preventDefault();
		    lbm.clearAlert();
		    return false;
		});
		alertPanel.removeClass("hidden");
	}
};

lbm.clearAlert = function(){
	alertPanel = $("#alert-panel");
	if(alertPanel){
		alertPanel.addClass("hidden");
		alertPanel.removeClass("alert-danger");
		alertPanel.removeClass("alert-success");
		alertPanel.empty();
	}
};


(
	function() {
		// Menu
		$("#my-menu").mmenu({
			   // options object
			}, {
			   // configuration object
		});
		
		this.$("#logout-button").tooltip({
			delay: 200,
			animation: true,
			toggle: "tooltip",
			placement: "left",
			title: "Logout"
		});
		
		this.$(".menu-trigger").tooltip({
			delay: 200,
			animation: true,
			toggle: "tooltip",
			placement: "right",
			title: "Click to see Menu"
		});
		
		// Feedback modal
		var FeedbackModel = Backbone.Model.extend({
			url: "/feedback"
		});
		
		var FeedbackModalView = Backbone.View.extend({
			el: ".feedback-container",

			events : {
				"change textarea" : "setValue",
				"click #submit-button" : "submitRequest",
			},
			
			showModal: function(){
				$("#feedback-modal").modal();
			},
			
			setValue: function(event){
				this.model.set("feedback", $("textarea").val());
				this.model.set("url", $(location).attr('href'));
			},

			submitRequest : function(event) {
				this.model.save({}, {
					error: function(model, xhr, options){
						alert("Problem submitting feedback: " + xhr.responseText);
					}
				});
				$("#feedback-modal").modal("hide");
			}
		});
		
		new FeedbackModalView({model: new FeedbackModel()});

})(jQuery);