{#

This file is part of the Sincerity JsDoc Template

Copyright 2011-2017 Three Crickets LLC.

The contents of this file are subject to the terms of the LGPL version 3.0:
http://www.gnu.org/copyleft/lesser.html

Alternatively, you can obtain a royalty free commercial license with less
limitations, transferable or non-transferable, directly from Three Crickets
at http://threecrickets.com/

#}
//
// This file was generated by the Sincerity JsDoc Template
//
// Sincerity is Copyright 2011-2017 Three Crickets LLC.
//
// See http://threecrickets.com/sincerity/
//

<if test="JSDOC.opt.u">var uniqueFileNames = {+ JSON.stringify(uniqueFileNames, null, '\t') +};
</if>
Ext.onReady(function() {
	Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));

	// Parse query arguments
	var args = {};
	var query = window.location.search.substring(1).split('&');
	for (var q in query) {
		var arg = query[q].split('=');
		args[unescape(arg[0])] = unescape(arg[1]);
	}

	function openSource(name) {
		var id = 'source-' + name;
		var view = Ext.getCmp(id);
		var tabs = Ext.getCmp('tabs');
		
		if (!view) {
			var url = 'source/' + name.replace(/\\.\\.?[\\\\\/]/g, '').replace(/[\\\\\/]/g, '_').replace(/\\:/g, '_') + '.html';
			view = Ext.create('Ext.panel.Panel', {
				id: id,
				title: name,
				autoScroll: true,
				closable: true,
				stateful: false,
				bodyCls: 'sincerity-source',
				loader: {
					url: url,
					/*ajaxOptions: {
						disableCaching: false
					},*/
					autoLoad: true,
					failure: function(loader) {
						loader.getTarget().destroy();
					}
				}
			});
			tabs.add(view);
		}

		tabs.setActiveTab(view);
	}

	function openNamespace(name, item) {
		var id = 'class-' + name;
		var view = Ext.getCmp(id);
		var tabs = Ext.getCmp('tabs');
		
		if (!view) {
			var url = 'namespace/' + <if test="JSDOC.opt.u">uniqueFileNames[name]<else />name</if> + '.html';
			view = Ext.create('Ext.panel.Panel', {
				id: id,
				title: name,
				autoScroll: true,
				closable: true,
				stateful: false,
				bodyCls: 'sincerity-class',
				loader: {
					url: url,
					/*ajaxOptions: {
						disableCaching: false
					},*/
					autoLoad: true,
					failure: function(loader) {
						loader.getTarget().destroy();
					},
					success: function(loader) {
						var view = loader.getTarget();
						view.highlight();
						
						view.getEl().select('span.open-namespace', true).on('click', function(event, el) {
							openNamespace(decodeURIComponent(el.getAttribute('namespace')), decodeURIComponent(el.getAttribute('item')));
						});

						view.getEl().select('div.open-source', true).on('click', function(event, el) {
							openSource(decodeURIComponent(el.getAttribute('source')));
						});

						view.getEl().select('div.menu', true).on('click', function(event, el) {
							var items = [];
							view.getEl().select(el.getAttribute('items')).each(function(el) {
								items.push({
									text: el.getAttribute('menu'),
									itemToHighlight: el.dom.id,
									iconCls: 'hide-icon',
									handler: function(item, event) {
										view.itemToHighlight = item.itemToHighlight;
										view.highlight();
									}
								});
							});
							if (items.length) {
								Ext.create('Ext.menu.Menu', {
									plain: true,
									items: items
								}).showAt(event.getXY());
							}
						});
					}
				},
				itemToHighlight: item,
				highlight: function() {
					if (this.itemToHighlight) {
						var item = Ext.get(this.itemToHighlight);
						if (item) {
							item.scrollIntoView(this.getEl().down('div.sincerity-class'));
							item.highlight('AAAAFF', {duration: 3000});
						}
						delete this.itemToHighlight;
					}
				}
			});
			tabs.add(view);
		}
		else if (item) {
			view.itemToHighlight = item;
			view.highlight();
		}

		tabs.setActiveTab(view);
	}
	
	Ext.create('Ext.data.TreeStore', {
		storeId: 'namespaces',
		proxy: {
			type: 'rest',
			url: 'data/namespaces.json',
			appendId: false
			//noCache: false
		},
		//autoLoad: true,
		listeners: {
			load: function() {
				if (args.namespace) {
					openNamespace(args.namespace, args.item);
				}
			}			
		}
	});

	Ext.create('Ext.data.Store', {
		storeId: 'search',
		fields: ['id'],
		data: []
	});

	Ext.create('Ext.container.Viewport', {
		id: 'viewport',
		layout: 'border',
		items: [{
			region: 'north',
			margin: '10 10 10 10',
			border: false,
			bodyCls: 'x-border-layout-ct', // Uses the neutral background color
			html: '<div class="title">{+ (JSDOC.opt.D.link ? '<a href="' + JSDOC.opt.D.link + '">' : '') + JSDOC.opt.D.title + (JSDOC.opt.D.link ? '</a>' : '') +}</div><div class="subtitle">Documentation generated by <a href="http://code.google.com/p/jsdoc-toolkit/">JsDoc</a> with the <a href="http://threecrickets.com/sincerity/">Sincerity Template</a></div>'
		}, {
			region: 'west',
			split: true,
			margin: '0 0 10 10',
			width: 300,
			layout: 'card',
			id: 'namespaces',
			items: [{
				xtype: 'treepanel',
				store: 'namespaces',
				border: false,
				autoScroll: true,
				useArrows: true,
				rootVisible: false,
				style: 'border: 0px',
				listeners: {
					itemclick: function(view, model, item, index) {
						openNamespace(model.get('id'));
					}
				}
			}, {
				xtype: 'dataview',
				store: 'search',
				border: false,
				autoScroll: true,
				tpl: '<tpl for="."><div class="search-item" id="search-{id}">{id}</div></tpl>',
				overItemCls: 'search-item-over',
				trackOver: true,
				itemSelector: 'div.search-item',
				emptyText: '<div class="search-item">Nothing found!</div>',
				listeners: {
					itemclick: function(view, model, item, index) {
						openNamespace(model.get('id'));
					}
				}
			}],
			tbar: {
				items: [{
					xtype: 'tbtext',
					text: 'Search:'
				}, {
					xtype: 'textfield',
					id: 'search',
					width: 150,
					listeners: {
						change: function(textfield, newValue, oldValue) {
							if (!newValue) {
								Ext.getCmp('namespaces').getLayout().setActiveItem(0);
								return;
							}
							
							newValue = new RegExp(newValue, 'i');
							var search = Ext.StoreManager.get('search');
							search.removeAll();
							
							function gather(node) {
								var id = node.get('id')
								if (newValue.test(id)) {
									search.add({id: id})
								}
								node.eachChild(gather);
							}
							
							Ext.StoreManager.get('namespaces').getRootNode().eachChild(gather);

							Ext.getCmp('namespaces').getLayout().setActiveItem(1);
						}
					}
				}, {
					text: 'Clear',
					handler: function() {
						Ext.getCmp('search').setValue('');
					}
				}]
			}
		}, {
			region: 'center',
			split: true,
			margin: '0 10 10 0',
			autoScroll: true,
			xtype: 'tabpanel',
			id: 'tabs'
		}]
	});
});