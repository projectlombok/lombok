var lombok = angular.module('kafedra', ['ngRoute']);
lombok.config(['$routeProvider', function($routeProvider) {
    $routeProvider.
        when('/credits', {
            templateUrl: 'views/credits.html'
        }).
        when('/features/stable', {
            templateUrl: 'views/features/stable.html'
        }).
        when('/features/experimental', {
            templateUrl: 'views/features/experimental.html'
        }).
        when('/feature/api/:code', {
            templateUrl: function(params) {
                return 'views/features/details/' + params.code + '.html'
            }
        }).
        when('/disable-checked-exceptions', {
            templateUrl: 'views/disable-checked-exceptions.html'
        }).
        when('/download', {
            templateUrl: 'views/download.html'
        }).
        when('/install', {
            templateUrl: 'views/install.html'
        }).
        when('/changelog', {
            templateUrl: 'views/changelog.html'
        }).
        when('/install/android', {
            templateUrl: 'views/install/android.html'
        }).
        when('/install/compilers', {
            templateUrl: 'views/install/compilers.html'
        }).
        when('/install/ide', {
            templateUrl: 'views/install/ide.html'
        }).
        when('/install/others', {
            templateUrl: 'views/install/others.html'
        }).
        otherwise({
            redirectTo: '/',
            templateUrl: 'views/main.html'
        });
}]).controller('MainController', function($scope, $location) {
    $scope.toFeature = function(code) {
        $location.path("/feature/api/" + code);
    }
}).directive('scrollOnClick', function() {
    return {
        restrict: 'A',
        link: function(scope, $elm, attrs) {
            var idToScroll = attrs.destination;
            $elm.on('click', function() {
                var $target;
                if (idToScroll) {
                    $target = $("#" + idToScroll);
                } else {
                    $target = $elm;
                }
                $("html,body").animate({scrollTop: $target.offset().top - 80});
            });
        }
    }
}).directive('feature', function() {
    return {
        restrict: 'E',
        replace: true,
        transclude: true,
        scope: {},
        link: function(scope, element, attributes) {
            scope.title = attributes["title"];
            scope.code = attributes["code"];
        },
        template: '<div class="bs-callout bs-callout-danger">' +
        '   <h4><a href="#feature/api/{{code}}"><code>{{title}}</code></a></h4>' +
        '   <p ng-transclude=""></p>' +
        '</div>'
    };
});
