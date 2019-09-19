var http = require('http');
var fs = require('fs');
var socketio = require('socket.io');

var app = http.createServer(function(request,response){
    var url = request.url;
    if(request.url == '/'){
      url = '/index.html';
    }
    if(request.url == '/favicon.ico'){
      response.writeHead(404);
      response.end();
      return;
    }
    response.writeHead(200);
    response.end(fs.readFileSync(__dirname + url));
 
}).listen(8000, function(){
    console.log('Server running at http://115.145.243.99:8000');
});

var io = socketio.listen(app);
io.sockets.on('connection', function (socket){
    console.log('Socket ID : ' + socket.id + ', Connect');
    socket.on('clientMessage', function(data){
        console.log('Client Message : ' + data);
        
        var output_data = data + '\n';
        var options = { encoding: 'utf8', flag: 'a+' };
        
        fs.writeFile('./output.csv', output_data, options, function(err){
            console.log('출력 완료');

       var message = {
            msg : 'server',
            data : 'data'
        };
        socket.emit('serverMessage', message);

        });
    });
});