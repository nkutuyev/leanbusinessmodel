(function() {
	
	var regionManager = new lbm.RegionManager({el: $(".container")})
	
	var Router = Backbone.Router.extend({
	    routes: {
	    	"": "showFeedback"
	    },
	    
	    showFeedback: function(){
	    	regionManager.show(new FeedbackContainerView());
	    }
	});
	
	var FeedbackTableView = Backbone.View.extend({
		
		el: ".feedback-data-container",
		
		template : Handlebars.compile($("#feedback-table-template").html()),
		
		render: function(){
			this.$el.html(this.template());
			this.initFeedbackGrid();
			return this;
		}, 
		
		initFeedbackGrid: function(){
			$('.footable').footable();
		}
	});
	
	// Uber Feedback View
	var FeedbackContainerView = Backbone.View.extend({
		
		tagName: "div",
		
		template : Handlebars.compile($("#feedback-container-template").html()),
		
		render: function(){
			this.$el.html(this.template());
			var tableView = new FeedbackTableView();
			tableView.render();
			return this;
		}
	});
	
	/*
	 * -------------------------------------------------------------------------
	 */
	
	var router = new Router();
	Backbone.history.start();
	
})(jQuery);
