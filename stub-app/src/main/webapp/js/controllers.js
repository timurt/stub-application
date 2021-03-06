angular.module('listApp', []);

var app = angular.module('editApp',
		[ 'ui.bootstrap', 'angularBootstrapNavTree' ], function(
				$locationProvider) {
			$locationProvider.html5Mode(true);
		});

function ListCtrl($scope, $http) {

	$http.get('http://localhost:8080/stub-app/soap/services', {
		cache : false
	}).success(function(data) {
		$scope.services = data;
	});

	$scope.deleteService = function($name) {
		if (confirm("Do you want delete service " + $name)) {
			$http.get('http://localhost:8080/stub-app/soap/delete/' + $name)
					.success(function(data) {

					});
			var oldServices = $scope.services;
			$scope.services = [];
			angular.forEach(oldServices, function(service) {
				if (service.text != $name)
					$scope.services.push(service);
			});
		}
	};
};

function EditCtrl($scope, $http, $location, $modal) {

	$scope.name = $location.search()['service'];

	$http(
					{
						url :'http://localhost:8080/stub-app/soap/service/' + $scope.name,

						method : 'GET',
						data : $scope.service,
						headers : {
							"Content-Type" : "application/json; charset=UTF-8",
							'Content-Transfer-Encoding': 'utf-8' 
						}

					}).success(function(data) {
$scope.service = data;
console.log(data);
			}).error(function(error) {

			});

	

	$scope.open = function($method) {

		var modalInstance = $modal.open({
			templateUrl : 'modal.html',
			controller : ModalInstanceCtrl,
			resolve : {

				method : function() {
					return $method;
				},
				name : function() {
					return $scope.name;
				}

			}
		});

		modalInstance.result.then(function() {

			$http(
					{
						url : 'http://localhost:8080/stub-app/soap/service/'
								+ $scope.name + '/save',

						method : 'POST',
						data : $scope.service,
						headers : {
							"Content-Type" : "application/json; charset=UTF-8"
						}

					}).success(function(response) {

			}).error(function(error) {

			});

		}, function() {
			$http.get(
					'http://localhost:8080/stub-app/soap/service/'
							+ $scope.name, {
						cache : false
					}).success(function(data) {
				$scope.service = data;
			});
		});
	};

};

app.directive("ngFileSelect", function() {

	return {
		link : function($scope, el) {

			el.bind("change", function(e) {

				$scope.file = (e.srcElement || e.target).files[0];

				$scope.getFile($scope.file, el.context.id);

			})

		}

	}

})

app
		.directive(
				'ngAutoTest',
				function() {

					return {
						restrict : 'A',
						scope : {
							ngAutoTest : '='
						},
						link : function(scope, element, attrs) {
							// console.log('I am at here 2');
							// set the initial value of the textbox
							element.val(scope.ngAutoTest);

							element.data('old-value', scope.ngAutoTest);

							function split(val) {
								return val.split(/ \s*/);
							}
							function extractLast(term) {
								return split(term).pop();
							}

							scope
									.$watch(
											'ngAutoTest',
											function(val) {
												element.val(scope.ngAutoTest

												);

												element
														.autocomplete({
															minLength : 0,
															source : function(
																	request,
																	response) {
																var vars = scope.$parent.method.variables;
																result = [];
																var last = extractLast(request.term);

																if (last != ''
																		&& last[0] == ':') {

																	for (var i = 0; i < vars.length; i++) {
																		// console.log(last.substr(1).toLowerCase()+'
																		// '+$scope.method.variables[i].key.substr(0,last.length-1).toLowerCase());
																		if ((last.length <= vars[i].key.length)
																				&& (last
																						.substr(
																								1)
																						.toLowerCase() == vars[i].key
																						.substr(
																								1,
																								last.length - 1)
																						.toLowerCase())) {
																			result
																					.push(vars[i].key);
																		}
																	}
																}

																response(result);
															},
															focus : function() {
																// prevent value
																// inserted on
																// focus
																return false;
															},
															select : function(
																	event, ui) {

																var terms = split(this.value);
																// remove the
																// current input
																terms.pop();
																// add the
																// selected item
																terms
																		.push(ui.item.value);

																// add
																// placeholder
																// to get the
																// comma-and-space
																// at the end
																terms.push("");
																this.value = terms
																		.join(" ");
																return false;
															}
														});
											});

							// on blur, update the value in scope
							element
									.bind(
											'propertychange keyup paste',
											function(blurEvent) {
												if (element.data('old-value') != element
														.val()) {
													// console.log('value
													// changed, new value is: '
													// + element.val());
													scope
															.$apply(function() {
																scope.ngAutoTest = element
																		.val();
																element
																		.data(
																				'old-value',
																				element
																						.val());
															});
												}
											});
						}
					};
				});

function ModalInstanceCtrl($scope, $modalInstance, $http, method, fileReader,
		name) {

	$scope.variable_data = [];

	$scope.variable_tree = {};
	$scope.output_data = [];

	$scope.putput_tree = {};

	$scope.method = method;
	if (method.variables == '' || method.variables == ' ') {
		$scope.method.variables = [];
	}
	if (method.cases == '' || method.cases == ' ') {
		$scope.method.cases = [];
	}
	$scope.outputs = [];
	$scope.requestTemplate = "";
	$scope.responseTemplate = "";

	$http.get(
			'http://localhost:8080/stub-app/soap/service/' + name
					+ '/grequest/' + method.name, {
				cache : false
			}).success(function(data) {

		$scope.requestTemplate = data;

		$scope.variable_data = xmlToJson($scope.requestTemplate);

		$scope.variable_tree = {};
	});

	$http.get(
			'http://localhost:8080/stub-app/soap/service/' + name
					+ '/gresponse/' + method.name, {
				cache : false
			}).success(function(data) {
		if ($scope.response != null) {
			$scope.output_data = xmlToJson(data);

			$scope.output_tree = {};
		}
		$scope.responseTemplate = data;
		$scope.output_data = xmlToJson($scope.responseTemplate);

		$scope.output_tree = {};

	});

	$scope.getFile = function(file, type) {

		$scope.file = file;

		fileReader
				.readData($scope.file, $scope)
				.then(
						function(result) {

							if (type == 'requestTemplate') {
								$scope.requestTemplate = result;
								$scope.variable_data = xmlToJson($scope.requestTemplate);

								$scope.variable_tree = {};

								$http(
										{
											url : 'http://localhost:8080/stub-app/soap/service/'
													+ name
													+ '/srequest/'
													+ method.name,

											method : 'POST',
											data : $scope.requestTemplate,
											headers : {
												"Content-Type" : "application/xml; charset=UTF-8"
											}

										}).success(function(response) {

								}).error(function(error) {

								});

							} else if (type == 'responseTemplate') {
								if ($scope.response != null) {
									$scope.output_data = xmlToJson(result);

									$scope.output_tree = {};
								}
								$scope.responseTemplate = result;
								$scope.output_data = xmlToJson($scope.responseTemplate);

								$scope.output_tree = {};

								$http(
										{
											url : 'http://localhost:8080/stub-app/soap/service/'
													+ name
													+ '/sresponse/'
													+ method.name,

											method : 'POST',
											data : $scope.responseTemplate,
											headers : {
												"Content-Type" : "application/xml; charset=UTF-8"
											}

										}).success(function(response) {

								}).error(function(error) {

								});

							} else {
								$scope.output_data = xmlToJson(result);

								$scope.output_tree = {};
								$scope.response = {
									name : file.name,
									file : result
								};

								$http(
										{
											url : 'http://localhost:8080/stub-app/soap/service/'
													+ name
													+ '/fresponse/'
													+ method.name
													+ "/"
													+ file.name,

											method : 'POST',
											data : $scope.response.file,
											headers : {
												"Content-Type" : "application/xml; charset=UTF-8"
											}

										}).success(function(response) {

								}).error(function(error) {

								});

							}
						});
	};

	$scope.beautyVariable = function(key) {
		return key.substr(1);
	}

	function beautyPath(path) {
		var str = "";
		var split = path.split("/");
		for (var i = 1; i < split.length; i++) {
			if (split[i].indexOf(":") !== -1) {
				var temp = split[i].split(":")[1];
				str += "/" + temp;
			} else {
				str += "/" + split[i];
			}
		}
		return str;
	}
	$scope.viewVariable = function(key) {
		for (var i = 0; i < $scope.method.variables.length; i++) {
			if ($scope.method.variables[i].key == key) {

				alert("KEY "+$scope.method.variables[i].key+"\nPATH "+$scope.method.variables[i].path);
				break;
			}
		}
	};

	$scope.createVariable = function(key) {
		var newVariable = {
			key : ":" + key,
			path : beautyPath($scope.selectedVariablePath)
		};

		$scope.method.variables.splice(0, 0, newVariable);
		$('#variablename').val('');

	};

	$scope.deleteVariable = function(key) {

		for (var i = 0; i < $scope.method.variables.length; i++) {
			if ($scope.method.variables[i].key == key) {

				$scope.method.variables.splice(i, 1);
				break;
			}
		}

	};

	$scope.initCase = function() {
		$scope.outputs = [];
	}
	$scope.viewCase = function(test) {
		for (var i = 0; i < $scope.method.cases.length; i++) {
			if ($scope.method.cases[i].test == test) {
				var s = "TEST "+$scope.method.cases[i].test+"\n";
				if ($scope.method.cases[i].file!=null) {
					s+="FILE "+$scope.method.cases[i].file.path;
				}
				s+="\n\nOUTPUTS\n";
				for (var j=0; j<$scope.method.cases[i].outputs.length;j++) {
					s+="PATH "+$scope.method.cases[i].outputs[j].path+"\nVALUE "+$scope.method.cases[i].outputs[j].value+"\n\n";
				}
				
				alert(s);
				break;
			}
		}
	};

	$scope.deleteCase = function(test) {
		for (var i = 0; i < $scope.method.cases.length; i++) {
			if ($scope.method.cases[i].test == test) {

				$scope.method.cases.splice(i, 1);
			}
		}
	}

	$scope.createCase = function(test) {

		newCase = {
			test : test,

			outputs : $scope.outputs
		};
		if ($scope.response != null) {
			newCase.file = {
				path : 'templates/' + method.name + '/responses/'
						+ $scope.response.name
			};
		} else {
			newCase.file = {
				path : 'templates/' + method.name + '/response.xml'
			};
		}
		$scope.response = null;
		$scope.method.cases.splice(0, 0, newCase);
		$scope.output_data = xmlToJson($scope.responseTemplate);

		$scope.output_tree = {};
		$('#responseFile').val('');
		$('#casename').val('');
		$scope.outputs = [];

	}
	$scope.createOutput = function(value) {
		var newOutput = {
			value : value,
			path : beautyPath($scope.selectedOutputPath)
		};

		$scope.outputs.splice(0, 0, newOutput);
		$('#outputname').val('');
	}

	$scope.viewOutput = function(value) {
		for (var i = 0; i < $scope.outputs.length; i++) {
			if ($scope.outputs[i].value == value) {

				alert("PATH "+$scope.outputs[i].path+"\nVALUE "+$scope.outputs[i].value);
				break;
			}
		}
	}

	$scope.deleteOutput = function(value) {
		for (var i = 0; i < $scope.outputs.length; i++) {
			if ($scope.outputs[i].value == value) {

				$scope.outputs.splice(i, 1);
			}
		}
	}

	$scope.ok = function() {

		$modalInstance.close();
	};

	$scope.cancel = function() {

		$modalInstance.dismiss('cancel');
	};

	$scope.variable_tree_handler = function(branch) {
		$scope.selectedVariablePath = branch.xpath;
	};

	$scope.output_tree_handler = function(branch) {
		$scope.selectedOutputPath = branch.xpath;
	};

	function xmlToJson(xml) {
		var data = [];
		var parser = new DOMImplementation();

		var domDoc = parser.loadXML(xml);

		var docRoot = domDoc.getDocumentElement();

		data.push(rec(docRoot));

		function rec(element, parentPath) {

			var result = {
				label : element.nodeName,
				children : [],
				xpath : parentPath + "/" + element.nodeName
			}
			for (var i = 0; i < element.childNodes.length; i++) {
				if (element.childNodes.item(i).nodeName.indexOf("#text") !== -1) {
					continue;
				}
				result.children.push(rec(element.childNodes.item(i),
						result.xpath));
			}
			return result;
		}
		return data;
	}

};

