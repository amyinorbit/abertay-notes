<?php
/**
 * request.php - contains information about the request that was sent to the
 * http server.
 * 
 * @version     1.0a1
 * @author      Amy Parent <amy@amyparent.com>
 * @package     http
 */
namespace http;

class request {
    
    private $_method;
    private $_headers;
    private $_url;
    private $_params;
    private $_body;
    
    private $_basic_username ;
    private $_basic_password;
    
    public function __construct() {
        $this->_headers = [];
        $this->_params = [];
        $this->_body = "";
        $this->_basic_username = null;
        $this->_basic_password = null;
        
        $this->_method = $_SERVER["REQUEST_METHOD"];
        $this->_headers = getallheaders();
        
        $body = file_get_contents("php://input");
        $this->_body = is_null($body) ? "" : $body;
        
        $this->_params = $this->_SafeInput($_GET);
        $this->_url = "/".trim(parse_url($_SERVER["REQUEST_URI"], PHP_URL_PATH), "/");
        $this->_ParseBasic();
    }
    
    /**
     * Returns the request's HTTP method.
     */
    public function Method() {
        return $this->_method;
    }
    
    /**
     * Returns the requested URL.
     */
    public function URL() {
        return $this->_url;
    }
    
    /**
     * Returns the request's body.
     */
    public function Body() {
        return $this->_body;
    }
    
    /**
     * Returns the query string parameter value for a key, or the given default
     * value if the key isn't present.
     */
    public function Query($key, $default = null) {
        if(!isset($this->_params[$key])) {
            return $default;
        }
        return $this->_params[$key];
    }
    
    /**
     * Returns the HTTP header value for a key, or the given default
     * value if the key isn't present.
     */
    public function Header($key, $default = null) {
        if(!isset($this->_headers[$key])) {
            return $default;
        }
        return $this->_headers[$key];
    }
    
    /**
     * Returns the HTTP Basic Auth username
     */
    public function BasicUser() {
        return $this->_basic_username;
    }
    
    public function BasicPassword() {
        return $this->_basic_password;
    }
    
    /**
     * Returns a tag-stripped, whitespace-trimmed array containing safe request
     * data.
     * @param   $input      The array to safe.
     * @return  A sanitised array.
     */
    private function _SafeInput($input) {
		$cleaned = Array();
		if(is_array($input)) {
			foreach($input as $k => $v) {
				$cleaned[$k] = $this->_SafeInput($v);
			}
		} else {
			$cleaned = is_string($input)? trim(strip_tags($input)) : $input;
		}
		return $cleaned;
    }
    
    /**
     * Parses the username and password if a HTTP Basic authentication header is sent
     */
    private function _ParseBasic() {
        $token = $this->Header("Authorization");
        if(is_null($token)) { return; }
        if(strpos(strtolower($token), "basic") !== 0) { return; }
        $token = base64_decode(substr($token, 6));
        $parts = explode(":", $token);
        if(count($parts) !== 2) { return; }
        list($this->_basic_username, $this->_basic_password) = $parts;
    }
    
}