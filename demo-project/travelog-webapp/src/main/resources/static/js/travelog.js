var m = angular.module('travelog', ['ngRoute', "ngFileUpload"]);

m.uuid = function () {
    var delim = "-";

    function S4() {
        return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
    }

    return 'i'+(S4() + S4() + delim + S4() + delim + S4() + delim + S4() + delim + S4() + S4() + S4());
};


m.config(function ($routeProvider,$httpProvider) {

    $httpProvider.defaults.useXDomain = true;
    $routeProvider
        .when('/intro', {
            templateUrl: '/components/intro.html',
            controller: 'IntroCtrl'
        })
        .when('/travelogs-list', {
            templateUrl: '/components/travelogs-list.html',
            controller: 'TravelogsListCtrl'
        })
        .when('/travelog-view/:id', {
            templateUrl: '/components/travelog-view.html',
            controller: 'TravelogViewCtrl'
        })
        .when('/travelog-edit/:id', {
            templateUrl: '/components/travelog-editor.html',
            controller: 'TravelogEditorCtrl'
        })
        .otherwise({redirectTo: '/intro'});
});

m.controller('IntroCtrl', function ($scope, $http) {

});

m.controller('TravelogViewCtrl', function ($scope, $http, $routeParams) {


    $http.get("/api/travelog/" + $routeParams.id).then(function (r) {
        $scope.tl = r.data._source;
    })

});

m.controller('TravelogsListCtrl', function ($scope, $http, $location) {

    $http.post("/api/travelog/search", {}).then(function (r) {
        $scope.travelogs = r.data;
    });

    $scope.createTravelog = function () {

        var id =  m.uuid();
        $http.put("/api/travelog/" +id,{id:id, title:"New Travelog"}).then(function (r) {
            $location.path("/api/travelog/" +id)
        });
    }

});

m.controller('TravelogEditorCtrl', function ($scope, $http, $routeParams, $location,$interval) {

    $scope.tl = {};

    $http.get("/api/travelog/" + $routeParams.id).then(function (r) {
        $scope.tl = r.data._source;
    });



    $scope.saveTravelog = function () {
        $http.put('/api/travelog/'+ $scope.tl.id, $scope.tl).then(function (r) {
            $location.path("/travelog-view/"+$scope.tl.id)
        }).catch(function (err) {
            console.error("Cannot save travelog "+ $scope.tl.id);
        })
    };

    $scope.refresh = function () {

        var images = $(".tl-image");

        angular.forEach(images, function (img) {
            var imgEl = $( img);
            var src = imgEl.attr('original-src');
            if(! src ) {

                $http.post("/api/assets/check-present", {url: src})
                    .then(function (r) {
                        console.info(r);
                        if (!imgEl.attr('loaded')) {
                            imgEl.attr('src', imgEl.attr('src') + '?t=' + new Date());
                            imgEl.attr('loaded', 'true');
                        }

                    }).catch(function (data, status, headers, config) {
                    imgEl.attr('src', '/img/ajax-loader-circle.gif');
                })
            }
        });


    };


    $scope.uploadFiles = function (files, errFiles) {
        if( ! $scope.tl.assets  ){
            $scope.tl.assets = [];
        }
        $scope.files = files;
        $scope.errFiles = errFiles;
        angular.forEach(files, function (file) {
            var contentType = file.type;
            file.inProgress = true;
            $http.post('/api/assets/generate-post-url/' + file.name, {"content-type": contentType,"travelogId":$scope.tl.id}).then(function (r) {
                var postUrl = r.data.url;

                $http.put(postUrl, file, {headers: {'Content-Type': contentType}}).then(function (r) {
                    console.log(JSON.stringify(r));
                    $scope.tl.assets.push( file.name );
                    file.inProgress = false;
                }).catch(function (resp) {
                    console.error("An Error Occurred Attaching Your File");
                    file.inProgress = false;
                });
            })

        });
        $interval($scope.refresh, 5000 );
    }
});

m.directive('s3file', function () {
    return {
        restrict: 'AE',
        scope: {
            file: '@'
        },
        link: function (scope, el, attrs) {
            el.bind('change', function (event) {
                var files = event.target.files;
                var file = files[0];
                scope.file = file;
                scope.$parent.file = file;
                scope.$apply();
            });
        }
    };
});

// https://s3-us-west-2.amazonaws.com/k8s-presentation-assets/1/thumbnails/DSC_8708-sm.jpg
m.httpAssetsBase = function(){
   return "https://s3-" + travelog.region + ".amazonaws.com/" + travelog.output_bucket + "/";
};

m.directive('travelogImage', function () {
   return {
       restrict: 'A',
       scope: {
           travelog:"=",
           travelogImage:"=",
           imgSize: "@"
       },

       link: function (scope, el, attrs) {

           var baseLink = scope.travelog.id + "/" + scope.travelogImage;

           var lnk = m.httpAssetsBase()+ baseLink;
           if( angular.isDefined(scope.imgSize)){
                var src = baseLink;
                var path = src.replace( /\/[^\/]+$/g,'/');
                var fn = src.replace( /.+\//g,'');
                var base = fn.replace( /[.].+$/g,'');
                lnk = m.httpAssetsBase()+ path + "thumbnails/" + base + "-"+ scope.imgSize + ".jpg"
           }
           el.attr("src", lnk);
           el.attr("original-src",lnk)
       }

   }
});
