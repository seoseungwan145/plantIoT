var http = require('http');
var fs = require('fs');
var socketio = require('socket.io');
let {PythonShell} = require('python-shell');
var async = require('async');

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
    var optionss = { encoding: 'utf8', flag: 'w' };
        
        fs.writeFile('./output.csv', "", optionss, function(err){
            console.log('출력 완료');
        });
});

var io = socketio.listen(app);
io.sockets.on('connection', function (socket){
    console.log('Socket ID : ' + socket.id + ', Connect');
    socket.on('clientMessage', function(data){
        console.log('Client Message : ' + data);

        if(data != 12){
            var output_data = data + '\n';
            var options = { encoding: 'utf8', flag: 'a' };
            
            fs.writeFile('./output.csv', output_data, options, function(err){
                console.log('출력 완료');

            /*
                var message = {
                    msg : 'server',
                    data : 'data'
                };
                socket.emit('serverMessage', message);
                */
            });

        }else
        {
            async.waterfall([
                function(callback) {
                var options = {
                    mode: 'text',
                    pythonPath: '',
                    pythonOptions: ['-u'],
                    scriptPath: '',
                    args: [1, 2, 3, 4, 5]
                };

                PythonShell.run('data_process.py',options,function(err,results){
                    if(err) throw err;
                    console.log('results: %j', results);
                    callback(null, results);
                });
                }], function(callback, items){
                    var optionss = {
                        mode: 'text',
                        pythonPath: '',
                        pythonOptions: ['-u'],
                        scriptPath: '',
                        args: [1, 2, 3, 4, 5]
                    };

                    PythonShell.run('linear_regression.py',optionss,function(err,results){
                        if(err) throw err;
                        console.log('results: %j', results);
                        var message = {
                            msg : 'server',
                            dust : results[0],
                            temp : results[1],
                            human : results[2],
                            sunny : results[3],
                            bias : results[4]
                        };
                        socket.emit('serverMessage', message); 
                    });
                });
        }
    });
});