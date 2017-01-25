(function() {
    //TEST

    function isTouchDevice() {
        return "ontouchstart" in window || navigator.msMaxTouchPoints > 0;
    }

    function initButtonText($scope) {
        var input = document.createElement("input");

        input.setAttribute("multiple", "true");

        if (input.multiple === true && !qq.android()) {
            $scope.uploadButtonText = "Select Files";
        }
        else {
            $scope.uploadButtonText = "Select a File";
        }
    }

    function initDropZoneText($scope, $interpolate) {
        if (qq.supportedFeatures.folderDrop && !isTouchDevice()) {
            $scope.dropZoneText = "Drop Files or Folders Here";
        }
        else if (qq.supportedFeatures.fileDrop && !isTouchDevice()) {
            $scope.dropZoneText = "Drop Files Here";
        }
        else {
            $scope.dropZoneText = $scope.$eval($interpolate("Press '{{uploadButtonText}}'"));
        }
    }

    function bindToRenderedTemplate($compile, $scope, $interpolate, element) {
        $compile(element.contents())($scope);

        initButtonText($scope);
        initDropZoneText($scope, $interpolate);
    }

    function openLargerPreview($scope, uploader, modal, size, fileId) {
        uploader.drawThumbnail(fileId, new Image(), size).then(function(image) {
            $scope.largePreviewUri = image.src;
            $scope.$apply();
            modal.showModal();
        });
    }

    function closePreview(modal) {
        modal.close();
    }

    var m = angular.module('travelog', []);

    m.controller('UploadCtrl', function ($scope, $http) {
        $scope.presignUrl = function(){
            $http.post("/api/assets/presign-upload/1test1/some.jpg").then(function (response) {
                $scope.presignedURL = response.data.url;
            })
        };

        $scope.uploadFile = function(){

            $http.post("/api/assets/presign-upload/1test1/"+$scope.file.name,{"content-type": $scope.file.type}).then(function (response) {
                $scope.presignedURL = response.data.url;

                $http.put($scope.presignedURL, $scope.file,{headers: {'Content-Type': 'image/jpeg'}}).then(function (r) {
                    console.log( JSON.stringify( r));
                }).catch(function(resp) {
                    console.error("An Error Occurred Attaching Your File");
                });
            })


        }
    });

    m.directive('s3file', function() {
        return {
            restrict: 'AE',
            scope: {
                file: '@'
            },
            link: function(scope, el, attrs){
                el.bind('change', function(event){
                    var files = event.target.files;
                    var file = files[0];
                    scope.file = file;
                    scope.$parent.file = file;
                    scope.$apply();
                });
            }
        };
    });


    m.directive("fineUploader", function($compile, $interpolate) {
        return {
            restrict: "A",
            replace: true,

            link: function($scope, element, attrs) {
                var endpoint = attrs.uploadServer,
                    notAvailablePlaceholderPath = attrs.notAvailablePlaceholder,
                    waitingPlaceholderPath = attrs.waitingPlaceholder,
                    acceptFiles = attrs.allowedMimes,
                    sizeLimit = attrs.maxFileSize,
                    largePreviewSize = parseInt(attrs.largePreviewSize),
                    allowedExtensions = JSON.parse(attrs.allowedExtensions),
                    previewDialog = document.querySelector('.large-preview'),

                    uploader = new qq.s3.FineUploader({
                        debug: true,
                        element: element[0],

                        request: {
                            endpoint: 'https://s3.amazonaws.com/mcy-travel-dev',
                            accessKey: 'AKIAIKPG36QE724PUOIQ'
                        },
                        signature: {
                            endpoint: '/api/assets/s3-sign-request',
                            customHeaders: {'Access-Control-Allow-Origin':'*'}
                        },
                        uploadSuccess: {
                            endpoint: '/api/assets/s3-upload-success'
                        },
                        objectProperties:{
                            bucket:'mcy-travel-dev',
                            key:function (id) {
                                return '1test1/'+ uploader.getName( id);
                            }
                        },


                        thumbnails: {
                            placeholders: {
                                notAvailablePath: notAvailablePlaceholderPath,
                                waitingPath: waitingPlaceholderPath
                            }
                        },

                        display: {
                            prependFiles: true
                        },

                        failedUploadTextDisplay: {
                            mode: "custom"
                        },

                        retry: {
                            enableAuto: true
                        },

                        chunking: {
                            enabled: true
                        },

                        resume: {
                            enabled: true
                        },

                        callbacks: {
                            onSubmitted: function(id, name) {
                                var fileEl = this.getItemByFileId(id),
                                    thumbnailEl = fileEl.querySelector('.thumbnail-button');

                                thumbnailEl.addEventListener('click', function() {
                                    openLargerPreview($scope, uploader, previewDialog, largePreviewSize, id);
                                });
                            }
                        }
                    });

                dialogPolyfill.registerDialog(previewDialog);
                $scope.closePreview = closePreview.bind(this, previewDialog);
                bindToRenderedTemplate($compile, $scope, $interpolate, element);
            }
        }
    });
})();
