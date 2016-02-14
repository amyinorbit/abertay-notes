<?php
/**
 * router.php - Simple router class that interfaces with http servers.
 * 
 * @version     1.0a1
 * @author      Cesar Parent <cesar@cesarparent.com>
 * @package     http
 */
namespace http;

require_once(__DIR__."/request.php");
require_once(__DIR__."/response.php");

class router {
    
    private $_routes;
    
    /**
     * Creates a new router.
     */
    public function __construct() {
        $_routes = [];
    }
    
    /**
     * Add a route handler to the dispatch table for GET requests.
     */
    public function onGet($route, callable $handler) {
        $this->route("GET", $route, $handler);
    }
    
    /**
     * Add a route handler to the dispatch table for POST requests.
     */
    public function onPost($route, callable $handler) {
        $this->route("POST", $route, $handler);
    }
    
    /**
     * Add a route handler to the dispatch table for PUT requests.
     */
    public function onPut($route, callable $handler) {
        $this->route("PUT", $route, $handler);
    }
    
    /**
     * Add a route handler to the dispatch table for DELETE requests.
     */
    public function onDelete($route, callable $handler) {
        $this->route("DELETE", $route, $handler);
    }
    
    /**
     * Add a route handler to the dispatch table.
     */
    public function route($method, $route, callable $handler) {
		$pattern = "/^".str_replace("/", "\/", $route)."$/";
        $this->_routes[$pattern][$method] = $handler;
    }
    
    /**
     * Dispatch a request to its handler. If no handler is registered for
     * the request's route and url, the response is set to 404 not found.
     */
    public function dispatch(request $request, response $response) {
        
        foreach($this->_routes as $pattern => $methods) {
            if (preg_match($pattern, $request->url(), $params)) {
				array_shift($params);
                foreach($methods as $method => $callback) {
                    if($method !== $request->method()) { continue; }
                    array_unshift($params, $request, $response);
                    $params = array_values($params);
                    call_user_func_array($callback, $params);
                    return;
                }
			}
        }
        $response->setStatusCode(404);
        $response->setHeader("Content-Type", "text/plain");
        $response->setBody("Not Found.");
    }
    
}