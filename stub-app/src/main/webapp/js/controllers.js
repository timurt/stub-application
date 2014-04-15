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
            alert(data);
        });
       var oldServices = $scope.services;
      $scope.services = [];
      angular.forEach(oldServices, function(service) {
        if (service.text != $name) $scope.services.push(service);
      });
    }
  };
};


function EditCtrl($scope, $http, $location, $modal, fileReader) {
    
  $scope.name= $location.search()['service'];
  
  $http.get('http://localhost:8080/stub-app/soap/service/'+$scope.name,{ cache: false}).
        success(function(data) {
          $scope.service = data;
        });
    

  

  $scope.items = ['item1', 'item2', 'item3'];

  $scope.open = function ($method) {

    var modalInstance = $modal.open({
      templateUrl: 'myModalContent.html',
      controller: ModalInstanceCtrl,
      resolve: {
        items: function () {
          return $scope.items;
        },
        method: function () {
          return $method;
        },
        fileReader: function () {
          return fileReader;
        }
      }
    });

    modalInstance.result.then(function (selectedItem) {
      $scope.selected = selectedItem;
    }, function () {
      
    });
  };

};

var ModalInstanceCtrl = function ($scope, $modalInstance, items, method, fileReader) {
  $scope.getFile = function () {
        $scope.progress = 0;

        fileReader.readData($scope.file, $scope)
                      .then(function(result) {
                          $scope.requestTemplate = result;
                      });
    };
 
    $scope.$on("fileProgress", function(e, progress) {
        $scope.progress = progress.loaded / progress.total;
    });      
  $scope.method = method;
  $scope.items = items;
  $scope.selected = {
    item: $scope.items[0]
  };

  $scope.ok = function () {
    $modalInstance.close($scope.selected.item);
  };

  $scope.cancel = function () {
    $modalInstance.dismiss('cancel');
  };
};

app.directive("ngFileSelect",function(){

  return {
    link: function($scope,el){
      
      el.bind("change", function(e){
      
        $scope.file = (e.srcElement || e.target).files[0];
        $scope.getFile();
      })
      
    }
    
  }
  
  
})