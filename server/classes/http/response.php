<?php
/**
 * response.php - Used to build a response that will be sent by an HTTP server.
 * 
 * @version     1.0a1
 * @author      Amy Parent <amy@amyparent.com>
 * @package     http
 */
namespace http;


class response {
    
    private $_headers;
    private $_body;
    private $_httpCode;
    
    // HTTP codes and reason-phrase map.
    private static $_codes = [
        100 => 'Continue',
        101 => 'Switching Protocols',
        200 => 'OK',
        201 => 'Created',
        202 => 'Accepted',
        203 => 'Non-Authoritative Information',
        204 => 'No Content',
        205 => 'Reset Content',
        206 => 'Partial Content',
        300 => 'Multiple Choices',
        301 => 'Moved Permanently',
        302 => 'Found',
        303 => 'See Other',
        304 => 'Not Modified',
        305 => 'Use Proxy',
        306 => '(Unused)',
        307 => 'Temporary Redirect',
        400 => 'Bad Request',
        401 => 'Unauthorized',
        402 => 'Payment Required',
        403 => 'Forbidden',
        404 => 'Not Found',
        405 => 'Method Not Allowed',
        406 => 'Not Acceptable',
        407 => 'Proxy Authentication Required',
        408 => 'Request Timeout',
        409 => 'Conflict',
        410 => 'Gone',
        411 => 'Length Required',
        412 => 'Precondition Failed',
        413 => 'Request Entity Too Large',
        414 => 'Request-URI Too Long',
        415 => 'Unsupported Media Type',
        416 => 'Requested Range Not Satisfiable',
        417 => 'Expectation Failed',
        422 => 'Unprocessable Entity',
        429 => 'Too Many Requests',
        500 => 'Internal Server Error',
        501 => 'Not Implemented',
        502 => 'Bad Gateway',
        503 => 'Service Unavailable',
        504 => 'Gateway Timeout',
        505 => 'HTTP Version Not Supported',
    ];
    
    public function __construct($code = 200) {
        $this->_headers = ["Content-Type" => "text/html"];
        $this->_httpCode = $code;
        $this->_body = "";
    }
    
    /**
     * Sets the Response's HTTP status code.
     */
    public function SetStatusCode($code) {
        if(!in_array($code, array_keys(self::$_codes))) {
            throw new \Exception("Invalid HTTP Status Code: ".$code);
        }
        $this->_httpCode = intval($code);
    }
    
    /**
     * Returns the Response's HTTP status code.
     */
    public function StatusCode() {
        return $this->_httpCode;
    }
    
    /**
     * Sets the value of an HTTP header field to send with the response.
     */
    public function SetHeader($header, $value) {
        if(!is_string($header) || !is_string($value)) {
            throw new \Exception("Invalid Header Data");
        }
        $this->_headers[$header] = $value;
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
     * Returns the Response's HTTP headers
     */
    public function Headers() {
        return $this->_headers;
    }
    
    /**
     * Sets the Respone's body. Replaces any existent data in the body.
     */
    public function SetBody($body) {
        $this->_body = $body;
    }
    
    /**
     * Appends data to the response's body. Has no effect if the body is
     * anything but a string.
     */
    public function Send($data) {
        if(!is_string($data) || !is_string($this->_body)) {
            return;
        }
        $this->_body .= $data;
    }
    
    /**
     * Returns the Response's body.
     */
    public function Body() {
        return $this->_body;
    }
    
    /**
     * Returns the HTTP status line header.
     */
    public function Status() {
        $status = self::$_codes[$this->_httpCode];
        return "HTTP/1.1 ".$this->_httpCode." ".$status;
    }
}