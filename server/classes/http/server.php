<?php
/**
 * server.php - simple http single-file server.
 * 
 * @version     1.0a1
 * @author      Amy Parent <amy@amyparent.com>
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
    
    /**
     * Creates a server with a main handler function.
     */
    public function __construct(callable $handler) {
        $this->_handler = $handler;
        $this->_request = new request();
        $this->_response = new response();
        
        $this->_filterOut = null;
    }
    
    /**
     * Processes the current request.
     */
    public function Start() {
        call_user_func($this->_handler, $this->_request, $this->_response);
        $this->_send();
    }
    
    /**
     * Registers a function that will be run before the response is sent.
     */
    public function FilterOut(callable $filter) {
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