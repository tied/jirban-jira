System.register(['angular2/core', 'angular2/router', '../../services/boardsService', '../../services/authenticationHelper'], function(exports_1) {
    var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
        var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
        if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
        else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
        return c > 3 && r && Object.defineProperty(target, key, r), r;
    };
    var __metadata = (this && this.__metadata) || function (k, v) {
        if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
    };
    var core_1, router_1, boardsService_1, authenticationHelper_1;
    var BoardsComponent;
    return {
        setters:[
            function (core_1_1) {
                core_1 = core_1_1;
            },
            function (router_1_1) {
                router_1 = router_1_1;
            },
            function (boardsService_1_1) {
                boardsService_1 = boardsService_1_1;
            },
            function (authenticationHelper_1_1) {
                authenticationHelper_1 = authenticationHelper_1_1;
            }],
        execute: function() {
            BoardsComponent = (function () {
                function BoardsComponent(boardsService, router) {
                    var _this = this;
                    this.boardsService = boardsService;
                    this.router = router;
                    boardsService.boardsData.subscribe(function (data) {
                        console.log('Boards: Got data' + JSON.stringify(data));
                        _this.boards = data;
                    }, function (err) {
                        console.log(err);
                        //TODO logout locally if 401, and redirect to login
                        //err seems to contain a complaint about the json marshalling of the empty body having gone wrong,
                        //rather than about the auth problems
                        //To be safe, go back to the error page
                        authenticationHelper_1.clearToken();
                        _this.router.navigateByUrl('/login');
                    }, function () { return console.log('Board: done'); });
                }
                BoardsComponent = __decorate([
                    core_1.Component({
                        selector: 'boards',
                        inputs: ['boards'],
                        providers: [boardsService_1.BoardsService]
                    }),
                    core_1.View({
                        templateUrl: 'app/components/boards/boards.html',
                        directives: [router_1.ROUTER_DIRECTIVES]
                    }), 
                    __metadata('design:paramtypes', [boardsService_1.BoardsService, router_1.Router])
                ], BoardsComponent);
                return BoardsComponent;
            })();
            exports_1("BoardsComponent", BoardsComponent);
        }
    }
});
//# sourceMappingURL=boards.js.map