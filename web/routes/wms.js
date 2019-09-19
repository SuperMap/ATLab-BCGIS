var express = require('express');
var router = express.Router();
var request = require('request');

/* GET home page. */
router.get('/', function(req, res, next) {
    request.get('http://127.0.0.1:8070/geoserver/rest/workspaces/testWS', (error, res0, body) => {
        res.render('wms', {content: res0.headers.accept});
    }).auth('admin', 'geoserver', false)
});

module.exports = router;
