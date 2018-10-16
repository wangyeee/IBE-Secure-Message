<?php
require_once 'DB.php';

/**
 * 新增传送用户
 * @param String $email 用户电子邮件地址
 * @param String $password SHA-1加盐摘要后的用户密码
 * @param Date $reg_time 用户注册时间
 */
function add_user($email, $password, $reg_time) {
	$db = new DB();
	$pdo = $db->get_connection();
	$sql = 'insert into IBE_USER values (null, :email, :password, :reg_time)';
	$stmt = $pdo->prepare($sql);
	$stmt->bindValue(':email', $email, PDO::PARAM_STR);
	$stmt->bindValue(':password', $password, PDO::PARAM_STR);
	$stmt->bindValue(':reg_time', $reg_time, PDO::PARAM_STR);
	$stmt->execute();
	$pdo = null;
}

/**
 * 检查用户电子邮件地址和密码是否匹配
 * @param String $email 电子邮件地址
 * @param String $password 明文密码
 * @return mixed 当且今当匹配时返回用户编号 否则返回false
 */
function check_user($email, $password) {
	$db = new DB();
	$pdo = $db->get_connection();
	$sql = 'select USER_ID, PASSWORD, REG_DATE from IBE_USER where EMAIL=:email limit 1';
	$stmt = $pdo->prepare($sql);
	$stmt->bindValue(':email', $email, PDO::PARAM_STR);
	$stmt->execute();
	$row = $stmt->fetch();
	if ($row) {
		$hash = $row['PASSWORD'];
		$reg_date = $row['REG_DATE'];
		$salt = str_ireplace(' ', '#', $reg_date);
		$actural = sha1($password . $salt);
		if (0 == strcasecmp($hash, $actural)) {
			$uid = $row['USER_ID'];
			$pdo = null;
			return $uid;
		}
	}
	$pdo = null;
	return false;
}
?>
