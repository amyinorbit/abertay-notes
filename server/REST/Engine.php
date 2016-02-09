<?php
/*
 * file:        RESTEngine.php
 * created:     2016-02-09
 * author:      Cesar Parent
 * package:     Basic Rest Server
 *
 * Base class for REST request and response handling
 */

namespace CesarParent\REST;

require_once(__DIR__."/Response.php");
require_once(__DIR__."/Exceptions.php");

class Engine {
    
    private static $valid_methods = Array(
        "GET",
        "POST",
        "PUT",
        "DELETE"
    );
    
    private $_method;
    private $_route;
    private $_data;
    private $_params;
    private $_headers;
    private $_routes;
    
    private $_options = Array(
        "base_url" => "http://localhost/",
        "timezone" => "Europe/London",
        "app_id" =
        "api_key" => "",
    );
    
    /**
     * Create a RESTEngine instance with the given options.
     *
     */
    public function __construct($options) {
        $this->_routes = Array();
        $this->_data = Array();
        $this->_params = Array();
        
        if(is_string($options)) {
            $raw = file_get_contents($options);
            $data = json_decode($raw, true);
            $this->_options = array_merge($this->_options, $data);
        }
        
        ini_set("always_populate_raw_post_data", -1);
        ini_set("log_error", 1);
        ini_set("error_log", __DIR__."/logs/php-"/*.time()*/.".log");
        date_default_timezone_set($this->_options["timezone"]);
        
        $this->_method = $this->_parse_method();
        $this->_data = $this->_parse_data();
        $this->_params = $this->_parse_query_params();
        $this->_headers = getallheaders();
        
        unset($this->_data["resource"]);
    }
    
    /**
     * Registers a route and its associated handler function. When a URL
     * matching the route is requested with the given HTTP method, the
     * callback will be invoked.
     * @param   $method     The HTTP Method to capture.
     * @param   $route      The route pattern to capture.
     * @param   $handler    The function called when a request is captured.
     */
    public function route($method, $route, callable $handler) {
        // Create a RegEx pattern for the route
		$pattern = "/^".str_replace("/", "\/", $route)."$/";
        $this->_routes[$pattern][$method] = $handler;
    }
    
    /**
     * Processes a request. The server's request superglobal is used to extract
     * request and route data. The response is sent to stdout.
     * @param   $url    The URL requested by the user.
     */
    public function execute($url) {
        
        // sanitise the URL request
        $this->_route = "/".trim($url, "/");
        
        $response = null;
        try {
            $response = $this->_match_route();
            
        } catch(RouteNotFoundException $e) {
            $response = Response::fail(400);
            $response->set_error_message("Invalid Route");
            
        } catch (MethodNotAllowedException $e) {
            $response = Response::fail(405);
            $response->set_error_message("Invalid HTTP method for endpoint.");
            
        } catch(InvalidParameterException $e) {
            $response = Response::fail(400);
            $response->set_error_message($e->getMessage());
            
        } catch(NotFoundException $e) {
            $response = Response::fail(404);
            $response->set_error_message($e->getMessage());
            
        } catch(\Exception $e) {
            // Log the error to the server logs.
            $response = Response::error("Server-side error");
            $log = "[".date("c")."] Exception: ".$e->getMessage()."\n".$e->getTraceAsString();
            file_put_contents(__DIR__."/logs/rest-"./*time().*/".log", $log);
        }
        finally {
            $this->_respond($response);
        }
    }
    
    /**
     * Returns a value from the request payload.
     * @param   $key        The key of the value to return.
     * @param   $default    The value to return if the key doesn't exist.
     * @return  The value for $key, or $default if it doesn't exist.
     */
    public function get_payload($key, $default = null) {
        if(!isset($this->_data[$key])) {
            return $default;
        }
        return $this->_data[$key];
    }
    
    /**
     * Returns the value of a request header.
     * @param   $header     The name of the header to return.
     * @param   $default    The header to return if the name doesn't exist.
     * @return  The header's content, or $default if it doesn't exist.
     */
    public function get_header($header, $default = null) {
        if(!isset($this->_headers[$header])) {
            return $default;
        }
        return $this->_headers[$header];
    }
    
    /**
     * Returns the value of a request query-string parameter.
     * @param   $key        The key of the value to return.
     * @param   $default    The value to return if the key doesn't exist.
     * @return  The parameter's value, or $default if it doesn't exist.
     */
    public function get_param($key, $default = null) {
        if(!isset($this->_params[$key])) {
            return $default;
        }
        return $this->_params[$key];
    }
    
    /**
     * Returns a value from the app's options
     * @param   $key        The key of the value to return.
     * @param   $default    The value to return if the key doesn't exist.
     * @return  The value for $key, or $default if it doesn't exist.
     */
    public function get_option($key, $default = null) {
        if(!isset($this->_options[$key])) {
            return $default;
        }
        return $this->_options[$key];
    }
    
    /**
     * Returns wether an API key is valid or not.
     * @param   $key        The key to validate.
     * @return  True if the API key is valid and belongs to an app, false otherwise.
     */
    public function validate_key($key) {
        if(!is_string($key)) { return false; }
        $app = array_search($key, $this->_options["apps"], true);
        return ($app !== false);
    }
    
    /**
     * Returns the request's HTTP method.
     * @return  The request's HTTP method.
     */
    public function get_method() {
        return $this->_method;
    }
    
    /**
     * Returns the request's URL route.
     * @return  The request's URL route.
     */
    public function get_route() {
        return $this->_route;
    }
    
    /**
     * Returns the client's IP address.
     * @return  The client's IP address.
     */
    public function get_ip() {
        return $_SERVER['REMOTE_ADDR'];
    }
    
    /**
     * Match the current route, and return the callback's response.
     */
    private function _match_route() {
        foreach($this->_routes as $pattern => $methods) {
            if (preg_match($pattern, $this->_route, $params)) {
				array_shift($params);
                foreach($methods as $method => $callback) {
                    if($method !== $this->_method) { continue; }
                    return call_user_func_array($callback, 
                                                array_values($params));
                }
                // No route for that method
                throw new MethodNotAllowedException($this->_method);
			}
        }
        // No route for that endpoint
        throw new RouteNotFoundException($this->_route);
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
    
    private function _parse_data() {
        if($this->_method === "POST" || $this->_method === "PUT") {
            $json =  json_decode(file_get_contents("php://input"), true);
            if(!is_null($json)) {
                return $this->_safe_input($json);
            }
        }
        return Array();
    }
    
    private function _parse_query_params() {
        $params = $_GET;
        unset($params["resource"]);
        return $this->_safe_input($params);
    }
    
    private function _parse_method() {
        $method = $_SERVER['REQUEST_METHOD'];
        if(!in_array($method, self::$valid_methods)) {
            throw new \Exception("Invalid method");
        }
        return $method;
    }
    
    private function _respond($response) {
        $data = $response->to_json()."\n";
        $response->set_header("Access-Control-Allow-Origin", "*");
        $response->set_header("Access-Control-Allow-Headers", "X-Abertay-Auth");
        $response->set_header("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE");
        $response->set_header("Content-Type", "application/json");
        $response->set_header("Content-Length", strlen(utf8_decode($data)));
        echo $data;
    }
    
    private function _send_headers($response) {
        header("HTTP/1.1 ".$response->get_http_code()." ".$response->get_http_phrase());
        foreach($response->get_headers() as $key => $value) {
            header($key.": ".$value);
        }
    }
    
}