    angular.module('listApp', []);

    var app = angular.module('editApp', ['ui.bootstrap'], function($locationProvider) {
      $locationProvider.html5Mode(true);
    });

    function ListCtrl($scope, $http) {

      $http.get('http://localhost:8080/stub-app/soap/services',{ cache: false}).
      success(function(data) {
        $scope.services = data;
      });

      

      $scope.deleteService = function($name) {
        if (confirm("Do you want delete service "+$name)) {
         $http.get('http://localhost:8080/stub-app/soap/delete/'+$name).
         success(function(data) {

         });
         var oldServices = $scope.services;
         $scope.services = [];
         angular.forEach(oldServices, function(service) {
          if (service.text != $name) $scope.services.push(service);
        });
       }
     };
   };


   function EditCtrl($scope, $http, $location, $modal) {
    $scope.name= $location.search()['service'];

    $http.get('http://localhost:8080/stub-app/soap/service/'+$scope.name,{ cache: false}).
    success(function(data) {
      $scope.service = data;
    });

    $scope.open = function ($method) {

      var modalInstance = $modal.open({
        templateUrl: 'modal.html',
        controller: ModalInstanceCtrl,
        resolve: {

          method: function () {
            return $method;
          }
        }
      });

      modalInstance.result.then(function (selectedItem) {
        $scope.selected = selectedItem;
      }, function () {

      });
    };


  };


  app.directive("ngFileSelectRequest",function(){

    return {
      link: function($scope,el){

        el.bind("change", function(e){

          $scope.file = (e.srcElement || e.target).files[0];

          $scope.getFile($scope.file,'request');

        })

      }

    }


  })

  app.directive("ngFileSelectResponse",function(){

    return {
      link: function($scope,el){

        el.bind("change", function(e){

          $scope.file = (e.srcElement || e.target).files[0];

          $scope.getFile($scope.file,'response');

        })

      }

    }


  })




  app.directive('myDirective', function () {

    return {
      restrict: 'A',
      scope: {
        myDirective: '='
      },
      link: function (scope, element, attrs) {
                  //console.log('I am at here 2');
                  // set the initial value of the textbox
                  element.val(scope.myDirective);
                
                  element.data('old-value', scope.myDirective);

                
                  function split( val ) {
                    return val.split( / \s*/ );
                  }
                  function extractLast( term ) {
                    return split( term ).pop();
                  }

                  scope.$watch('myDirective', function (val) {
                    element.val(scope.myDirective);
                   

                    element.autocomplete({
                      minLength: 0,
                      source: function( request, response ) {
                          var vars = scope.$parent.method.variables;
                          result = [];
                          var last = extractLast( request.term );




                          if (last != '' && last[0]==':') {

                            for (var i=0;i<vars.length;i++) {
                                      //console.log(last.substr(1).toLowerCase()+' '+$scope.method.variables[i].key.substr(0,last.length-1).toLowerCase());
                                      if ((last.length<=vars[i].key.length) && (last.substr(1).toLowerCase() == vars[i].key.substr(1,last.length-1).toLowerCase())) {
                                        result.push(vars[i].key);  
                                      }
                                    }
                                  }

                                  response(result);
          },
          focus: function() {
                          // prevent value inserted on focus
                          return false;
                        },
                        select: function( event, ui ) {
                        
                          var terms = split( this.value );
                          // remove the current input
                          terms.pop();
                          // add the selected item
                          terms.push( ui.item.value );

                          // add placeholder to get the comma-and-space at the end
                          terms.push( "" );
                          this.value = terms.join( " " );
                          return false;
                        }
                      });
  });

                  // on blur, update the value in scope
                  element.bind('propertychange keyup paste', function (blurEvent) {
                    if (element.data('old-value') != element.val()) {
                          //console.log('value changed, new value is: ' + element.val());
                          scope.$apply(function () {
                            scope.myDirective = element.val();
                            element.data('old-value', element.val());
                          });
                        }
                      });
                }
              };
            });






    function ModalInstanceCtrl($scope, $modalInstance, method, fileReader) {

      $scope.getFile = function (file,type) {

        $scope.file = file;
        $scope.progress = 0;

        fileReader.readData($scope.file, $scope)
        .then(function(result) {

          if (type == 'request') {
            $scope.requestTemplate = result;
          } else {
            $scope.responseTemplate = result;
          }
        });
      };

      $scope.beautyVariable = function(key) {
        return key.substr(1);
      }
      $scope.viewVariable = function(key) {
        

          for (var i=0;i<$scope.method.variables.length;i++) {
            if ($scope.method.variables[i].key==key) {

              $scope.method.variables[i].key += '1';
              break;
            }
          }
        
      

    };


    $scope.createVariable = function(key,path) {
      var newVariable = {
           key: ":"+key,
           path: path
         };
         $scope.method.variables.splice(0,0,newVariable);
         $('#prependedInput').val('');

  };

    $scope.deleteVariable = function(key) {

      for (var i=0;i<$scope.method.variables.length;i++) {
        if ($scope.method.variables[i].key==key) {

          $scope.method.variables.splice(i,1);
          break;
        }
      }

    };

    $scope.viewCase = function(test) {

    };

    $scope.deleteCase = function(test) {
      for (var i=0;i<$scope.method.cases.length;i++) {
        if ($scope.method.cases[i].test==test) {

          $scope.method.cases.splice(i,1);
        }
      }
    }

    $scope.createCase = function() {
      test = $('#casename').val();
      $('#casename').val('');
      newCase = {
        test : test,
        file : '',
        outputs : []
      };
       $scope.method.cases.splice(0,0,newCase);
       
    }

    $scope.method = method;


    $scope.ok = function () {
      $modalInstance.close($scope.selected.item);
    };

    $scope.cancel = function () {
      $modalInstance.dismiss('cancel');
    };



          };

