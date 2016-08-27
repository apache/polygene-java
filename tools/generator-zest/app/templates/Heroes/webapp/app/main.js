"use strict";
var browser_1 = require('angular2/platform/browser');
var router_1 = require('angular2/router');
var hero_service_1 = require('./hero.service');
var app_component_1 = require('./app.component');
browser_1.bootstrap(app_component_1.AppComponent, [
    router_1.ROUTER_PROVIDERS,
    hero_service_1.HeroService
]);
/*
Copyright 2016 Google Inc. All Rights Reserved.
Use of this source code is governed by an MIT-style license that
can be found in the LICENSE file at http://angular.io/license
*/ 
//# sourceMappingURL=main.js.map