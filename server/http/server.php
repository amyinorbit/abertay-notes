<?php
/**
 * Request.php - contains information about the request that was sent to the
 * http server.
 * 
 * @version     1.0a1
 * @author      Cesar Parent <cesar@cesarparent.com>
 * @package     http
 */
namespace http;
require_once(__DIR__."/request.php");
require_once(__DIR__."/response.php");

function createServer(callable $handler) {
    return new server($handler);
}


class server {
    
    private $_request;
    private $_response;
    
    private $_handler;
    
    private $_filterOut;
    private $_filterIn;
    
    public function __construct(callable $handler) {
        $this->_handler = $handler;
        $this->_request = new request();
        $this->_response = new response();
        
        $this->_filterOut = null;
    }
    
    public function start() {
        call_user_func($this->_handler, $this->_request, $this->_response);
        $this->_send();
    }
    
    public function filterOut(callable $filter) {
        $this->_filterOut = $filter;
    }
    
    private function _send() {
        // apply a filter if there's one
        if(is_callable($this->_filterOut)) {
            call_user_func($this->_filterOut, $this->_response);
        }
        
        $body = $this->_response->body();
        $len = strlen(utf8_decode($body));
        header($this->_response->status());
        header("Content-Length: ".$len);
        foreach($this->_response->headers() as $k => $v) {
            header($k.": ".$v);
        }
        echo $body;
    }
}