"use strict";

(function() {
	if (!String.prototype.trim) {
		String.prototype.trim = function () {
			return this.replace(/^[\s\uFEFF\xA0]+|[\s\uFEFF\xA0]+$/g, '');
		};
	}
	
	var imgDataUrl = null;
	
	function updateLocationInfo() {
		var locationType = $("input:radio[name='locationType']:checked").val();
		$("#locationType_usa").toggle(locationType === "usa");
		$("#locationType_eu").toggle(locationType === "eu");
		$("#locationType_other").toggle(locationType === "other");
		$("#paymentMethod_iban").toggle(locationType === "eu");
		if ($("#paymentMethod_iban input").is(":checked") && locationType !== "eu") $("#paymentMethod_stripe input").prop("checked", true);
		if ($("#paymentMethod_intl input").is(":checked") && locationType === "eu") $("#paymentMethod_iban input").prop("checked", true);
		$("#paymentMethod_intl").toggle(locationType !== "eu");
	}
	
	function updatePriceIndication() {
		$("#onlyYearlyWarning").hide();
		var x = getPriceIndication();
		if (x === "") {
			$("#costIndicator").text("").hide();
		} else {
			$("#costIndicator").text(x).show();
		}
	}
	
	function getPriceIndication() {
		try {
			var seats = parseInt($("#seats").val());
			if (isNaN(seats) || seats < 1) return "";
			var licenseType = $("input:radio[name='licenseType']:checked").val();
			var costPer;
			if (licenseType === "enterprise") costPer = 5;
			else if (licenseType === "professional") costPer = 2;
			else return "";
			var periodRaw = $("input:radio[name='paymentType']:checked").val();
			if (periodRaw === "monthly") {
				if (costPer*seats < 50) {
					$("#onlyYearlyWarning").show();
					return "Your license will cost €" + (costPer*seats*12) + ",- per year.";
				}
				return "Your license will cost €" + (costPer*seats) + ",- per month.";
			}
			return "Your license will cost €" + (costPer*seats*12) + ",- per year.";
		} catch (e) {
			return "";
		}
	}
	
	function showError(elemName) {
		var elem = $("#" + elemName + "Err");
		elem.fadeIn();
		$("#" + elemName).on("change keyup", function() {
			elem.hide();
		});
	}
	
	function applyLicense() {
		var submitButton = $("#submit");
		var spinner = $("<img>").attr("src", "/img/spinner.gif").attr("alt", "submitting").css("float", "left").css("margin-right", "20px");
		
		submitButton.on("click", function(evt) {
			evt.preventDefault();
			
			var onSuccess = function() {
				$("#orderHelp").hide();
				var okMsg = $("<div>").addClass("formSubmitOk").html("Thank you for ordering a Project Lombok license! We'll send your bill via email. If you have any further questions please contact us at <a href=\"mailto:orders@projectlombok.org\"><code>orders@projectlombok.org</code></a>.");
				spinner.replaceWith(okMsg);
			};
			
			var onFailure = function() {
				$("#orderHelp").hide();
				var errMsg = $("<div>").addClass("formSubmitFail").html("Our order form appears to be broken. Could you do us a favour and contact us at <a href=\"mailto:orders@projectlombok.org\"><code>orders@projectlombok.org</code></a>? Thank you!");
				spinner.replaceWith(errMsg);
			};
			
			var data = {};
			data.name = $("#companyName").val();
			data.email = $("#email").val();
			data.licenseType = $("input:radio[name='licenseType']:checked").val();
			data.seats = parseInt($("#seats").val());
			data.paymentType = $("input:radio[name='paymentType']:checked").val();
			data.mentionMe = $("#mentionMe").prop("checked");
			data.companyUrl = $("#companyUrl").val();
			data.locationType = $("input:radio[name='locationType']:checked").val();
			data.euVat = $("#euVat").val();
			data.paymentMethod = $("input:radio[name='paymentMethod']:checked").val();
			
			var formFail = false;
			
			if (!data.paymentMethod) {
				showError("paymentMethod");
				formFail = true;
			}
			
			if (!data.euVat && data.locationType === 'eu') {
				$("#euVat").focus();
				showError("euVat");
				formFail = true;
			}
			
			if (!data.locationType) {
				showError("locationType");
				formFail = true;
			}
			
			if (!data.seats || isNaN(data.seats) || data.seats < 10) {
				$("#seats").focus();
				showError("seats");
				formFail = true;
			}
			
			if (!data.paymentType) {
				showError("paymentType");
				formFail = true;
			}
			
			if (data.email.indexOf('@') === -1) {
				$("#email").focus();
				showError("email");
				formFail = true;
			}
			
			if (data.name.trim().length < 1) {
				$("#companyName").focus();
				showError("companyName");
				formFail = true;
			}
			
			if (formFail) return;
			
			var rnd = generateRandom();
			var err = processImageUpload();
			if (err) {
				alert(err);
				$("#logo")[0].value = null;
				return;
			}
			if (imgDataUrl) data.logo = imgDataUrl;
			
			submitButton.replaceWith(spinner);
			
			$.ajax({
				url: "/license-submit/" + rnd,
				method: "PUT",
				contentType: "application/json",
				data: JSON.stringify(data),
				processData: false,
				success: onSuccess,
				error: onFailure
			});
		});
		
		$("#companyLogo").on("click", function(evt) {
			evt.preventDefault();
			$("#logo").click();
		});
		
		$("#deleteCompanyLogo").on("click", function(evt) {
			evt.preventDefault();
			$("#logo")[0].value = null;
			$("#logoCnt").empty().hide();
			$("#companyLogo").show();
			$("#deleteCompanyLogo").hide();
		});
		
		$("#logo").on("change", function() {
			var err = showImage();
			if (err) {
				alert(err);
				$("#logo")[0].value = null;
			} else {
				$("#companyLogo").hide();
				$("#deleteCompanyLogo").show();
			}
		});
		
		$("#seats,.paymentType,.licenseType").on("change keyup", function() {
			updatePriceIndication();
		});
		$(".locationType").on("change", function() {
			updateLocationInfo();
		});
	}
	
	function generateRandom() {
		var buf = new Uint8Array(40);
		window.crypto.getRandomValues(buf);
		return btoa(String.fromCharCode.apply(null, buf)).replace(/\+/g, '_').replace(/\//gi, '-');
	}
	
	function showImage() {
		$("#logoCnt").empty().hide();
		try {
			return processImageUpload(function(dataUrl) {
				var img = $("<img />");
				img.css({
					"max-width": "500px",
					"max-height": "300px"
				});
				$("#logoCnt").append(img).show();
				img.attr("src", dataUrl);
			});
		} catch (e) {
			if (console && console.log) console.log(e);
		}
	}
	
	function processImageUpload(fnc) {
		var f = $("#logo")[0].files[0];
		if (!f) return;
		if (f.size > 10000000) return "Logo too large; please give us a logo below 10MiB in size.";
		var imageType = /^image\//;
		if (!imageType.test(f.type)) return "Please upload an image, for example in PNG format.";
		var reader = new FileReader();
		reader.onload = function(e) {
			imgDataUrl = e.target.result;
			if (fnc) fnc(e.target.result);
		};
		reader.readAsDataURL(f);
		return null;
	}
	
	$(applyLicense);
})();
