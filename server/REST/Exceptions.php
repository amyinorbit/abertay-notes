<?php
/*
 * file:        Exceptions.php
 * created:     2016-02-09
 * author:      Cesar Parent
 * package:     Basic Rest Server
 *
 * REST-specific exceptions
 */

namespace CesarParent\REST;

class MethodNotAllowedException extends \Exception {
    public function __construct($method) {
        parent::__construct($method." Not Allowed");
    }
}

class RouteNotFoundException extends \Exception {
    public function __construct($url) {
        parent::__construct($url." Not Found");
    }
}

class NotFoundException extends \Exception {
    public function __construct($resource, $id) {
        parent::__construct("No ".$resource." with id ".$id);
    }
}

class InvalidParameterException extends \Exception {
    public function __construct(array $params) {
        parent::__construt("Wrong parameters: ".explode(", ", $params)." allowed");
    }
}