<?php
/*
 * file:        Response.php
 * created:     2016-02-09
 * author:      Cesar Parent
 * package:     Abertay Voting - Backend
 *
 * Response class for the basic REST engine.
 */

namespace CesarParent\REST;

class Response {
    
    // Status strings for responses, only used internally
    const RESPONSE_SUCCESS = "success";
    const RESPONSE_FAIL    = "fail";
    const RESPONSE_ERROR   = "error";
    
    // HTTP headers that will be sent with the response
    private $_headers;
    // Response payload and metadata
    private $_data;
    // Response HTTP code
    private $_http_code;
    // The response's status
    private $_status;
    
    // HTTP codes and reason-phrase map.
    private static $_codes = Array(
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
    );
    
    /**
     * Private constructor - Response should be built using ::success, ::fail
     * and ::error.
     *
     * @param   $status     The response's status, SUCCESS, FAIL or ERROR.
     * @param   $code       The respone's HTTP status code.
     */
    private function __construct($status, $code) {
        $this->_headers = Array();
        $this->_data = Array();
        $this->_status = $status;
        $this->_data["status"] = $status;
        $this->_http_code = $code;
    }
    
    /**
     * Creates a response that denotes a successful request.
     *
     * @param   $code       The response's HTTP code, 200 by default.
     * @return  A new success Response object.
     */
    public static function success($code = 200) {
        $response = new Response(self::RESPONSE_SUCCESS, $code);
        return $response;
    }
    
    /**
     * Creates a response that denotes a failed request.
     *
     * @param   $code       The response's HTTP code, 400 by default.
     * @return  A new failure Response object.
     */
    public static function fail($code = 400, $message = null) {
        $response = new Response(self::RESPONSE_FAIL, $code);
        if(is_string($message)) {
            $response->_data["message"] = $message;
        } else {
            $response->_data["message"] = $response->get_http_phrase();
        }
        return $response;
    }
    
    /**
     * Creates a response that denotes a server error.
     *
     * @param   $message    An error message to be sent to the user.
     * @return  A new server-error Response object.
     */
    public static function error($message) {
        $response = new Response(self::RESPONSE_ERROR, 500);
        $response->_data["message"] = $message;
        return $response;
    }
    
    /**
     * For Success and Fail respones, adds a key-value pair to the response
     * payload.
     *
     * @param   $key        The key used to address the piece of data.
     * @param   $data       The piece of data to add to the payload.
     */
    public function put_payload($key, $data) {
        if($this->_status === self::RESPONSE_ERROR) { return; }
        $this->_data["data"][$key] = $data;
    }
    
    /**
     * For Fail and Server Error responses, adds a metadata-leve message to
     * the response.
     *
     * @param   $message    An error message to send in the response's metadata.
     */
    public function set_error_message($message) {
        if($this->_status === self::RESPONSE_SUCCESS) { return; }
        $this->_data["message"] = "$message";
    }
    
    /**
     * Adds a HTTP header field to the response.
     *
     * @param   $name       The HTTP header's name.
     * @param   $value      The HTTP header's value.
     */
    public function set_header($name, $value) {
        $this->_headers[$name] = $value;
    }
    
    /**
     * Returns a hashmap of the response's HTTP headers.
     *
     * @return  The response's headers.
     */
    public function get_headers() {
        return $this->_headers;
    }
    
    /**
     * Returns the HTTP status code of the response.
     *
     * @return  The HTTP status code of the response.
     */
    public function get_http_code() {
        return $this->_http_code;
    }
    
    /**
     * Returns the HTTP status phrase of the response.
     *
     * @return  The HTTP status phrase of the response.
     */
    public function get_http_phrase() {
		return self::$_codes[$this->_http_code];
    }
    
    /**
     * Returns a JSON representation of the response's body.
     *
     * @return  A JSON string terminated by a newline character.
     */
    public function to_json($options = null) {
        if(!is_null($options)) {
            return json_encode($this->_data, $options);
        }
        return json_encode($this->_data, JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT);
    }
    
    /**
     * Returns an XML representation of the response's body.
     *
     * @return  An XML string terminated by a newline character.
     */
    public function to_xml() {
        return xml_encode($this->_data, "response")."\n";
    }
}