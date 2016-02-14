<?php
/**
 * request.php - contains information about the request that was sent to the
 * http server.
 * 
 * @version     1.0a1
 * @author      Cesar Parent <cesar@cesarparent.com>
 * @package     http
 */
namespace http;

class Request {
    
    private $_method;
    private $_headers;
    private $_url;
    private $_params;
    private $_body;
    
    public function __construct() {
        $this->_headers = [];
        $this->_params = [];
        $this->_body = "";
        
        $this->_method = $_SERVER["REQUEST_METHOD"];
        $this->_headers = getallheaders();
        
        $body = file_get_contents("php://input");
        $this->_body = is_null($body) ? "" : $body;
        
        $this->_params = $this->_safe_input($_GET);
        $this->_url = parse_url($_SERVER["REQUEST_URI"], PHP_URL_PATH);;
    }
    
    /**
     * Returns the request's HTTP method.
     */
    public function method() {
        return $this->_method;
    }
    
    /**
     * Returns the requested URL.
     */
    public function url() {
        return $this->_url;
    }
    
    /**
     * Returns the request's body.
     */
    public function body() {
        return $this->_body;
    }
    
    /**
     * Returns the query string parameter value for a key, or the given default
     * value if the key isn't present.
     */
    public function query($key, $default = null) {
        if(!isset($this->_params[$key])) {
            return $default;
        }
        return $this->_params[$key];
    }
    
    /**
     * Returns the HTTP header value for a key, or the given default
     * value if the key isn't present.
     */
    public function header($key, $default = null) {
        if(!isset($this->_headers[$key])) {
            return $default;
        }
        return $this->_headers[$key];
    }
    
    /**
     * Return a tag-stripped, whitespace-trimmed array containing safe request
     * data.
     * @param   $input      The array to safe.
     * @return  A sanitised array.
     */
    private function _safe_input($input) {
		$cleaned = Array();
		if(is_array($input)) {
			foreach($input as $k => $v) {
				$cleaned[$k] = $this->_safe_input($v);
			}
		} else {
			$cleaned = is_string($input)? trim(strip_tags($input)) : $input;
		}
		return $cleaned;
    }
    
}