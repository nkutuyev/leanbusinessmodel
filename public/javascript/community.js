(function() {
	
	var regionManager = new lbm.RegionManager({el: $(".container")})
	
	var Router = Backbone.Router.extend({
	    routes: {
	    	"": "showCommunityView"
	    },
	    
	    showCommunityView: function(){
	    	regionManager.show(new CommunityContainerView());
	    }
	});
	
	var CommunityTableView = Backbone.View.extend({
		
		el: ".community-data-container",
		
		template : Handlebars.compile($("#community-table-template").html()),
		
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
	var CommunityContainerView = Backbone.View.extend({
		
		tagName: "div",
		
		template : Handlebars.compile($("#community-container-template").html()),
		
		render: function(){
			this.$el.html(this.template());
			var tableView = new CommunityTableView();
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

