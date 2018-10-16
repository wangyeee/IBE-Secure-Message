<?php
class DB {
	function __construct() {
	}

	function get_connection() {
		$db_ip = '127.0.0.1';
		$db_name = 'ibe_relay';
		$db_user = 'root';
		$db_password = 'secret';
		$dsn = 'mysql:host='.$db_ip. ';dbname=' . $db_name;
		$dbh = new PDO($dsn, $db_user, $db_password, array(
				PDO::ATTR_PERSISTENT => true,
				PDO::MYSQL_ATTR_USE_BUFFERED_QUERY => false
		));
		return  $dbh;
	}
}
?>
