// Copyright 2018-2020 Elimu Informatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

var BASE_URl = "/a2d2-api/api/cds/rule/";
var selectedServiceName = null;
var selectedRuleId = null;

$(document).ready(function() {
	$('#1').click(function() {
		$('#rulesresponse').css("display", "block");
		$.get(BASE_URl + 'get/pro-recommend', function(resp) {
			$('#response').val(resp);

		});
	});
	$('.dropdown-submenu a.test').on("click", function(e){
	    $(this).next('ul').toggle();
	    e.stopPropagation();
	    e.preventDefault();
	    selectedServiceName = $(this).text();
	    selectedRuleId = $(this).next('ul').find('a').attr('id');
	 });

	$('#save').click(function() {
		$("#restart").attr("disabled", "disabled");
		var ruledata = $('#response').val();
		$.ajax({
			type : "POST",
			url : BASE_URl + 'update',
			dataType : 'text',
			contentType : "application/json; charset=utf-8",
			data : JSON.stringify({
				'serviceName' : selectedServiceName,
				'resourceTextCode' : ruledata,
				'resourceType': 'drl',
				'resourceFile' : 'rules',
				'id' : selectedRuleId
			}),
			success : function(response) {
				if (response === 'success') {
					$("#success").fadeIn();
					setTimeout(function() {
		            $("#success").fadeOut(300)
					}, 2000);
					
				}
	    		 $("#restart").removeAttr("disabled");
			},
			error : function(data) {
				alert("Oops! Save unsuccessful");
				console.log(data);
			},
		});
	});
	$("#restart").click(function() {
	
		$.ajax({
			type : "PUT",
			url : "/a2d2-api/api/v1/cds-crud/" + selectedRuleId + "/reload",
			dataType : 'text',
			success : function(data) {
					$("#restartalert").fadeIn();
					setTimeout(function() {
			        $("#restartalert").fadeOut(300)
					}, 2000);
			},
			error : function(data) {
				alert("Oops! Application restarting fails ");
				console.log(data);
			},
		});
		
	});
});
