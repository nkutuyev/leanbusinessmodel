(function() {
	
	var regionManager = new lbm.RegionManager({el: $(".container")});
	
	var Router = Backbone.Router.extend({
	    routes: {
	    	"": "models",
	        "model/:modelId": "showCanvas",
	        "model/presentation/:modelId": "slideShow"
	    },
	    
	    models: function(){
	    	regionManager.show(new ModelContainerView());
	    },
	    
	    showCanvas: function(modelId){
	        var model = new BusinessModel({id: modelId});
	        model.fetch({
	        	success: function(){
		        	regionManager.show(new CanvasView({model: model}));
		        },
		        error: function(model, xhr, options){
					lbm.showAlert(xhr.responseText);
				}
		    });
	    },
	    
	    slideShow: function(modelId){
	        var businessModel = new BusinessModel({id: modelId});
	        businessModel.fetch({
	        	success: function(){
		        	regionManager.show(new SliderView({model: businessModel, collection: businessModel.getItemCollection()}));
		        },
		        error: function(model, xhr, options){
					lbm.showAlert(xhr.responseText);
				}
		    });
	    }
	});
	
	/**
	 * Model List view -----------------------------------------------------
	 */
	
	var BusinessModelDescriptor = Backbone.Model.extend({
		idAttribute: "collectionId",
		
		setEmptyName: function(){
			var name = this.attributes.name;
			this.attributes.name = name ? name : "Untitled";
		}
	
	});
	
	var ModelDescriptorCollection = Backbone.Collection.extend({
		url : "/collection",
		model : BusinessModelDescriptor
	});
	
	var modelCollection = new ModelDescriptorCollection(modelString);

	// The View for a Business Mode
	var ModelDescriptorView = Backbone.View.extend({

		tagName : "div",
		className: "canvas-list-cell",
		
		events: {
			"click #trash" : "remove",
			"click #share-icon" : "shareModal",
			"click #model-name" : "open"
		},

		template : Handlebars.compile($("#model-template").html()),
		
		shareModal: function(event) {
			var shareModelView = new ShareModelView({model: new ShareModel({id: this.model.id})});
			shareModelView.render();
		},

		remove : function(event) {
			lbm.clearAlert();
			this.model.destroy({
				wait: true,
				error: function(model, xhr, options){
					lbm.showAlert(xhr.responseText);
				}
			});
		},

		render : function() {
			this.$el.html(this.template(this.model.toJSON()));
			var shared = this.model.get("shared");
			var titleClass = "model-title";
			if(shared){
				titleClass = "model-title-shared";
				this.$("#trash").remove();
			}
			this.$("#model-title").addClass(titleClass);
			this.$("#share-icon").tooltip({
				delay: 200,
				animation: true,
				toggle: "tooltip",
				placement: "top",
				title: "Share Model"
			});
			return this;
		},
		
		open : function(event) {
			// open model
			router.navigate("model/" + this.model.get("modelId"), {trigger: true});
		}
	});
	
	var ShareModel = Backbone.Model.extend({
		initialize: function(){
			this.url = "/collection/" + this.id + "/share";
		}
	});
	
	// Share model view
	var ShareModelView = Backbone.View.extend({
		tagName: "div",

		events : {
			"change input" : "setEmail",
			"click #share-button" : "submitRequest",
			"click #cancel-button" : "closeModal"
		},

		template : Handlebars.compile($("#share-model-template").html()),
		
		render : function() {
			lbm.clearAlert();
			this.$el.html(this.template());
			contentContainerElement.append(this.$el);
			$("#share-model-modal").modal();
			$('#share-model-modal').on('hidden.bs.modal',{view: this}, this.closeView);
			return this;
		},
		
		setEmail: function(event){
			this.model.set("email", $("input").val());
		},

		submitRequest : function(event) {
			this.model.save({}, {
				success: function() {
					lbm.showAlert("Invitation to share the model has been sent to the member.", "alert-info");
				}, 
				error: function(model, xhr, options){
					lbm.showAlert(xhr.responseText);
				}
			});
			this.closeModal(event);
		},

		closeModal : function(event) {
			$("#share-model-modal").modal("hide");
		},
		
		closeView: function(event){
			event.data.view.close();
		}

	});

	// Create button view
	var CreateModelView = Backbone.View.extend({
		el : ".control-panel",

		events : {
			"click #create-button" : "create"
		},

		create : function(event) {
			// add new empty model
			lbm.clearAlert();
			modelCollection.create({},{ success : function(model, response) { 
													router.navigate("model/" + model.get("modelId"), {trigger: true});
												  },
										error: function(model, xhr, options){
											lbm.showAlert(xhr.responseText);
										}
						   	  		   });
		}

	});

	// Model List View
	var ModelListView = Backbone.View.extend({
		el : ".canvas-container",

		initialize : function() {
			// Listen for the reset, remove events on the collection.
			this.listenTo(this.collection, "reset", this.render);
			this.listenTo(this.collection, "remove", this.render);
		},

		render : function() {
			this.$el.empty();
			this.collection.each(function(businessModel) {
				// render individual models
				businessModel.setEmptyName();
				var modelView = new ModelDescriptorView({model : businessModel});
				this.$el.append(modelView.render().$el);
			}, this);

			// initialize wall grid
			$(".canvas-container").each(function() {
				var wall = new freewall(this);
				wall.reset({
					selector : ".canvas-list-cell",
					cellW: "auto",
					cellH: "auto",
					gutterY : 20,
					gutterX : 20
				});
				wall.fitWidth();
			});
			
			return this;
		}
	});
	
	// Uber content View
	var ModelContainerView = Backbone.View.extend({
		
		tagName: "div",
		
		template : Handlebars.compile($("#model-cotainer-template").html()),
		
		render: function(){
			this.$el.html(this.template());
			new CreateModelView({collection: modelCollection});
			var listView = new ModelListView({collection: modelCollection});
			listView.render();
			return this;
		}
	});
	
	/*---------------------------------------------------------------------
	 * Full Business Model model and view
	 */
	
	
	var NewVersion = Backbone.Model.extend({
		initialize : function(attr) {
			this.url = "/collection/" + attr.collectionId + "/model";
		}
	});
	
	var newVersion;
	
	// Create button view
	var CreateNewVersionView = Backbone.View.extend({
		el : "#toolbar",

		events : {
			"click #new-version-button" : "popupVersionForm"
		},

		initialize : function(options) {
			newVersion = new NewVersion(options);
			this.listenTo(newVersion, "sync", this.routeToNewVersion);
			
			$("#new-version-button").tooltip({
				delay: 400,
				animation: true,
				toggle: "tooltip",
				placement: "top",
				title: "Copy model to create new version"
			});
			
			$('#version-field').editable({
			    title: 'Enter New Version Name',
			    mode: 'popup',
			    type: 'text',
			    placement: 'top',
			    toggle: 'manual',
			    emptytext: '',
			    display: false,
			    success: function(response, newValue) {
			    	lbm.clearAlert();
			        newVersion.set('version', newValue); //update backbone model
			        newVersion.save({}, {
						error: function(model, xhr, options){
							lbm.showAlert(xhr.responseText);
						}
			        });
			    }
			});
		},

		popupVersionForm : function(event) {
			event.stopPropagation();
		    $('#version-field').editable('toggle');
		}, 
		
		routeToNewVersion: function(event) {
			router.navigate("model/" + newVersion.get("modelId"), {trigger: true});
		}

	});

	
	var ModelVersion = Backbone.Model.extend({});
	
	var ModelVersionCollection = Backbone.Collection.extend({
		model : ModelVersion,
		initialize : function(modelCollectionId) {
			this.url = "/collection/" + modelCollectionId + "/version";
		}
	});
	
	var ModelVersionView = Backbone.View.extend({
		
		el: "#version-selector",

		template : Handlebars.compile($("#version-selector-template").html()),
		
		events: {
	        "change select": "changeVersion"
	    },
		
		initialize : function(options) {
			this.listenTo(this.collection, "reset", this.renderOptions);
			this.currentModelId = options.currentModelId;
		},
		
		render: function(){
			this.$el.html(this.template());
			this.collection.fetch({reset: true,
				error: function(model, xhr, options){
					lbm.showAlert(xhr.responseText);
				}
			});
			return this;
		},
		
		renderOptions: function(){
			this.collection.each(function(modelVersion) {
				// render individual version values
				var isSelected = (this.currentModelId == modelVersion.id) ? " selected='true' " : "";
				$("select").append("<option " + isSelected + " value='" + modelVersion.id + "'>" + modelVersion.get("version") + "</option>");
			}, this);
		},
		
		changeVersion: function(event){
			event.stopPropagation();
			router.navigate("model/" + event.target.value, {trigger: true});
		}
	});
	
	var SetVersionModel = Backbone.Model.extend({
		idAttribute: "collectionId",
		urlRoot: "/collection"
	});
	
	var SetVersionView = Backbone.View.extend({
		el: "#set-current-version",
		
		events: {
	        "click": "setCurrentVersion"
	    },
		
		initialize : function(options) {
			this.$el.tooltip({
				delay: 200,
				animation: true,
				toggle: "tooltip",
				placement: "top",
				title: "Set this version as default"
			});
		},
	    
		setCurrentVersion: function(event){
			event.preventDefault();
			lbm.clearAlert();
			this.model.save({},{
				success: function() {
					lbm.showAlert("Default version of the model was successfully updated", "alert-info");
				},
				error: function(model, xhr, options){
					lbm.showAlert(xhr.responseText);
				}
			});
		}
		
	});
	
	var BusinessModel = Backbone.Model.extend({
		urlRoot: "/model",
		
		getItemCollection: function(){
			var items = [
			   new ModelItem({title: "Problem", content: (this.get("problem") || "") + 
				   										 "\n\nExisting Alternatives:\n\n" + 
				   										 (this.get("existingAlternatives") || "")}),
			   new ModelItem({title: "Customer Segments", content: (this.get("customerSegments") || "") +
				   													"\n\nEarly Adopters:\n\n" + 
				   													(this.get("earlyAdopters") || "")}),
			   new ModelItem({title: "Value Proposition", content: (this.get("valueProposition") || "") +
				   													"\n\nHigh-level Concept:\n\n" + 
				   													(this.get("highLevelConcept") || "")}),
			   new ModelItem({title: "Solution", content: this.get("solution")}),
			   new ModelItem({title: "Unfair Advantage", content: this.get("unfairAdvantage")}),
			   new ModelItem({title: "Revenue Streams", content: this.get("revenueStreams")}),
			   new ModelItem({title: "Cost Structure", content: this.get("costStructure")}),
			   new ModelItem({title: "Key Metrics", content: this.get("keyMetrics")}),
			   new ModelItem({title: "Channels", content: this.get("channels")}),
			];
			return new ModelItemCollection(items);
		}
	});
	
	var CanvasView = Backbone.View.extend({
		
		tagName: "div",

		template : Handlebars.compile($("#canvas-container-template").html()),
		
		events: {
	        "click #slider-button": "showSliderView",
	        "click #print-button": "printCanvas",
	        "click #share-modal-button": "showShareModal"
	    },
		
		render: function(){
			this.$el.html(this.template(this.model.toJSON()));
			var modelId = this.model.get("id");
			enableCanvas(modelId);
			var modelCollectionId = this.model.get("collectionId");
			var versionSelectorView = new ModelVersionView({collection: new ModelVersionCollection(modelCollectionId), currentModelId: modelId});
			versionSelectorView.render();
			new CreateNewVersionView({"collectionId":modelCollectionId, "modelId" : modelId});
			new SetVersionView({model: new SetVersionModel({currentVersionId: modelId, collectionId: modelCollectionId})});
			return this;
		},
		
		showSliderView: function(){
			router.navigate("model/presentation/" + this.model.id, {trigger: true});
		},
		
		printCanvas: function(){
			var options = { mode : "popup",popHt: 900, popWd: 900, popX: 0, popY: 0, retainAttr: ["id","class","style"] };
			this.$("#canvas-container").printArea(options);
		},
		
		showShareModal: function(event) {
			event.stopPropagation();
			var shareModelView = new ShareModelView({model: new ShareModel({id: this.model.get("collectionId")})});
			shareModelView.render();
		},
	});
	
	var enableCanvas = function(modelId) {
		
		$("#name").tooltip({
			delay: 200,
			animation: true,
			toggle: "tooltip",
			placement: "top",
			title: "Click to change"
		});
		
		$("#slider-button").tooltip({
			delay: 200,
			animation: true,
			toggle: "tooltip",
			placement: "top",
			title: "Presentation View"
		});
		
		$("#share-modal-button").tooltip({
			delay: 200,
			animation: true,
			toggle: "tooltip",
			placement: "top",
			title: "Share Model"
		});
		
		// Canvas tiles
		$(".tCanvas").each(function() {
			var wall = new freewall(this);
			wall.reset({
				selector: '.col-small',
				cellW: 200,
				cellH: 'auto',
				fixSize: 0,
				gutterY: 0,
				gutterX: 0,
				onResize: function() {
					wall.fitWidth();
				}
			})
			wall.fitWidth();
		});
		
		$(".bCanvas").each(function() {
			var bWall = new freewall(this);
			bWall.reset({
				selector: '.col-wide',
				cellW: 500,
				cellH: 'auto',
				fixSize: 0,
				gutterY: 0,
				gutterX: 0,
				onResize: function() {
					bWall.fitWidth();
				}
			})
			bWall.fitWidth();
		});
		
		// for scroll bar appear;
		$(window).trigger("resize");
		
		// Editable fields
		$.fn.editable.defaults.mode = 'inline';
		$.fn.editable.defaults.type = 'textarea';
		$.fn.editable.defaults.rows = 10;
		$.fn.editable.defaults.url = '/model/' + modelId + '/field';
		$.fn.editable.defaults.showbuttons = 'bottom';
		$.fn.editable.defaults.inputclass = "form-control";
		$.fn.editable.defaults.ajaxOptions = {contentType: "application/json", processData: false};
		$.fn.editable.defaults.params = function(params) {return JSON.stringify(params);};
		
		$('#name').editable({
			type: "text",
			title: "Model Name: ",
			inputclass: "canvas-name-input",
			showbuttons: false,
			onblur: "submit",
			emptytext: "Untitled (Click to change)"
		});

		$('#problem').editable({
			title: "Problem Definition",
			emptytext: "Write down up to three top customer problems you're trying to resolve."
		});
		
		$('#existingAlternatives').editable({
			title: "Alternatives",
			emptytext: "Are customers currently use alternative approaches to solve this problems? No matter how bad they are."
		});
		
		$('#solution').editable({
			title: "Proposed Solution",
			emptytext: "Briefly describe top three features of you solution."
		});
		
		$('#keyMetrics').editable({
			title: "Metrics",
			emptytext: "Key results/activities you will measure to evaluate your ideas."
		});
		
		$('#valueProposition').editable({
			title: "Value Propsosition",
			emptytext: "Why your solutions are different from current alternatives?"
		});
		
		$('#highLevelConcept').editable({
			title: "Concept",
			emptytext: "'Elevator Speach' for your idea."
		});
		
		$('#unfairAdvantage').editable({
			title: "Advantage",
			emptytext: "Why is it going to be hard to replicate your idea?"
		});
		
		$('#channels').editable({
			title: "Channels",
			emptytext: "How are you going to reach your customers?"
		});
		
		$('#customerSegments').editable({
			title: "Customer Segments",
			emptytext: "Your target customers, segmented into groups."
		});
		
		$('#earlyAdopters').editable({
			title: "Early Adopters",
			emptytext: "Do you have customers who could buy/try your product or service early?"
		});
		
		$('#costStructure').editable({
			title: "Cost Structure",
			rows: 5,
			emptytext: "Primary cost categories."
		});
		
		$('#revenueStreams').editable({
			title: "Revenue Streams",
			rows: 5,
			emptytext: "Revenue Model, Gross Margins, Lifetime Value, etc."
		});
	}
	
	/*
	 * End of canvas model and view
	 --------------------------------------------------------------------------*/
	
	/*
	 * Slider View -------------------------------------------------------------
	 */
	
	var ModelItem = Backbone.Model.extend({
	
	});
	
	var ModelItemCollection = Backbone.Collection.extend({
		model : ModelItem
	});
	
	var SlideView = Backbone.View.extend({
		
		tagName: "div",

		template : Handlebars.compile($("#slide-template").html()),
		
		initialize: function(){
			this.$el.html(this.template(this.model.toJSON()));
		}
	});
	
	var SliderView = Backbone.View.extend({
		
		tagName: "div",

		template : Handlebars.compile($("#slider-template").html()),
		
		render: function(){
			this.$el.html(this.template(this.model.toJSON()));
			this.collection.each(function(slideModel) {
				// render individual slides
				var slideView = new SlideView({model : slideModel});
				this.$('.slider').append(slideView.render().$el);
			}, this);
			$('.slider').AnySlider({
				interval: 0,
				bullets: false
			});
			return this;
		}
	});
	
	/*
	 * -------------------------------------------------------------------------
	 */
	
	var router = new Router();
	Backbone.history.start();
	
})(jQuery);
