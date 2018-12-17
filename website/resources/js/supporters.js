"use strict";

(function($) {
	var supporters = {};
	var weights = {};
	var types = ["enterprise", "professional", "patron"];
	
	function shuffle(array) {
		var i = 0, j = 0, temp = null;
		for (i = array.length - 1; i > 0; i -= 1) {
			j = Math.floor(Math.random() * (i + 1));
			temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
	}
	
	function pad(number) {
		return number < 10 ? '0' + number : number;
	}
	
	function fromDate(d) {
		return d.getUTCFullYear() + '-' + pad(d.getUTCMonth() + 1) + '-' + pad(d.getUTCDate());
		
	}
	
	function toDate(s) {
		var x = /^(\d{4})-(\d{2})-(\d{2})$/.exec(s);
		if (x) return new Date(parseInt(x[1]), parseInt(x[2]) - 1, parseInt(x[3]));
		return null;
	}
	
	function Supporter(type, json) {
		this.type = type;
		this.name = json.name;
		this.logo = json.logo;
		this.url = json.url;
		this.showName = json.showName;
		this.start = json.range ? toDate(json.range[0]) : null;
		this.end = json.range ? toDate(json.range[1]) : null;
		this.weight = (!json.weight && json.weight !== 0.0) ? 1.0 : json.weight;
	}
	
	Supporter.prototype.inRange = function(d) {
		return (!this.start || this.start <= d) && (!this.end || this.end > d);
	};
	
	Supporter.prototype.render = function() {
		var d = $("<div />").addClass("supportItem").addClass(this.type);
		var a = d;
		if (this.url) {
			a = $("<a />").attr("href", this.url).attr("rel", "noopener").attr("target", "_blank");
			d.append(a);
		}
		var n = $("<span />").text(this.name);
		a.append(n);
		
		if (this.logo) {
			a.addClass("logo");
			var i = new Image();
			var showName = this.showName;
			i.onload = function() {
				var w = i.width;
				var h = i.height;
				var wf = w / 162;
				var hf = h / 80;
				var f = hf > wf ? hf : wf;
				var wt = Math.round(w / f);
				var ht = Math.round(h / f);
				i.width = wt;
				i.height = ht;
				var ji = $(i);
				if (!showName) a.empty();
				ji.css("width", wt + "px");
				ji.css("height", ht + "px");
				ji.attr("alt", n.text());
				ji.attr("title", n.text());
				a.prepend(ji);
			};
			i.src = '/files/' + this.logo;
		}
		return d;
	}
	
	function errorHandler(xhr, statusText, err) {
		var errMsg = "Can't connect to projectlombok.org to fetch the list of licensees and supporters.";
		if (console && console.log) {
			console.log("AJAX error for loading list of supporters:");
			console.log(err);
		}
		var errBox = $("<div />").addClass("errorBox").text(errMsg);
		$(".supporters").text("").append(errBox);
	}
	
	function successHandler(data) {
		$.each(types, function() {
			var t = this;
			supporters[t] = [];
			if (data && data[t]) $.each(data[t], function() {
				supporters[t].push(new Supporter(t, this));
			});
		});
		weights = data.modWeight;
		if (typeof weights !== 'object') weights = {};
		updatePage();
	}
	
	function build() {
		var spinner = $("<img />").attr("title", "loading").attr("src", "/img/spinner.gif").addClass("spinner");
		$(".supporters").append(spinner);
		$.ajax({
			url: "/files/supporters.json",
			dataType: "json",
			cache: true,
			error: errorHandler,
			success: function(data) {
				spinner.remove();
				successHandler(data);
			}
		});
	}
	
	function applySupporters() {
		build();
	}
	
	function updatePage() {
		updateSupporters();
		updateSupporterBar();
	}
	
	var supPerBar = 4;
	function updateSupporterBar() {
		var s = $(".supporterBar");
		if (s.length === 0) return;
		s.find(".introText").show();
		s.append($("<div />").addClass("sbCnt"));
		var sf = s.find(".supporterFooter").show();
		s.append(sf);
		s = s.find(".sbCnt");
		var now = new Date();
		var list = [];
		$.each(types, function() {
			var t = this;
			$.each(supporters[t], function() {
				if (this.inRange(now)) {
					var w = weights[t] ? weights[t] : 1.0;
					if (this.weight) w = w * this.weight;
					for (var i = 0; i < w; i++) list.push(this);
				}
			});
		});
		
		shuffle(list);
		
		var len = list.length;
		if (!len) return;
		
		var pos = 0;
		var c = [], cd = [];
		for (var i = 0; i < supPerBar; i++) {
			c[i] = null;
			cd[i] = $("<div />").addClass("barItem");
		}
		
		var upd = function() {
			var nw = [], a = [], fo = $(), fi = $();
			var sPos = pos;
			for (var i = 0; i < supPerBar; i++) {
				if (++pos === len) pos = 0;
				var z = false;
				for (var j = 0; j < i; j++) if (nw[j] === list[pos]) z = true;
				if (z) i--;
				else nw[i] = list[pos];
				if (pos === sPos) break;
			}
			for (var i = 0; i < supPerBar; i++) a[i] = (nw[i] === c[i] || !nw[i]) ? null : nw[i].render(i + 1).hide();
			for (var i = 0; i < supPerBar; i++) {
				if (a[i]) {
					fi = fi.add(a[i]);
					fo = fo.add(cd[i].children());
					if (cd[i].parent().length === 0) s.append(cd[i]);
					cd[i].append(a[i]);
					c[i] = nw[i];
				}
			}
			
			if (fo.length === 0) fi.fadeIn();
			else {
				fo.fadeOut("normal", function() {
					fi.fadeIn();
					fo.remove();
				});
			}
		};
		
		setInterval(upd, 10000);
		upd();
	}
	
	function updateSupporters() {
		var s = $(".supporters");
		s.empty();
		var now = new Date();
		$.each(types, function() {
			var t = this;
			var d = $("<div />").addClass("row");
			var h = $("<h2 />").text(t);
			d.append(h);
			$.each(supporters[t], function() {
				if (this.inRange(now)) d.append(this.render());
			});
			if (d.children().length > 1) s.append(d);
		});
		if (s.children().length < 1) {
			var x = $("<div />").addClass("noSupportersBox").html(
				"We don't have any supporters yet this month.<br /><a href=\"https://patreon.com/lombok\">Become a patron</a> " +
				"or <a href=\"/order-license-info\">order a professional or enterprise license</a> today!");
			s.append(x);
		}
	}
	
	$(applySupporters);
})($);