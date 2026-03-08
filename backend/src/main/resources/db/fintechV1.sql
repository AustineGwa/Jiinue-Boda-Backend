-- MySQL dump 10.13  Distrib 8.0.34, for Linux (x86_64)
--
-- Host: 159.223.194.128    Database: fintech
-- ------------------------------------------------------
-- Server version	8.0.33-0ubuntu0.22.10.2

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admin_signatories`
--

DROP TABLE IF EXISTS `admin_signatories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_signatories` (
  `num` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `app_id` int DEFAULT NULL,
  `notification_number` varchar(50) DEFAULT NULL,
  `notification_email` varchar(100) DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`num`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_signatories`
--

LOCK TABLES `admin_signatories` WRITE;
/*!40000 ALTER TABLE `admin_signatories` DISABLE KEYS */;
INSERT INTO `admin_signatories` VALUES (1,3,1,'254797301935','magnifiquensengimana@jiwezeshe.com',NULL,'2023-07-22 15:10:17','2023-07-22 15:10:17'),(2,10,1,'254718728894','austinegwa@jiwezeshe.com',NULL,'2023-07-22 15:11:18','2023-07-22 15:11:18');
/*!40000 ALTER TABLE `admin_signatories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `asset_attachments`
--

DROP TABLE IF EXISTS `asset_attachments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asset_attachments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `doc_type` varchar(255) NOT NULL,
  `doc_name` varchar(255) NOT NULL,
  `asset_id` int NOT NULL,
  `doc_path` varchar(255) NOT NULL,
  `public_url` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `asset_attachments`
--

LOCK TABLES `asset_attachments` WRITE;
/*!40000 ALTER TABLE `asset_attachments` DISABLE KEYS */;
INSERT INTO `asset_attachments` VALUES (40,'ASSET-DOCUMENT','f939vIxUTxcXfHyra0drRKS7ybhfhURXpgkSLGD1.pdf',13,'/storage/assetsUploads/f939vIxUTxcXfHyra0drRKS7ybhfhURXpgkSLGD1.pdf','http://127.0.0.1:8000/storage/assetsUploads/f939vIxUTxcXfHyra0drRKS7ybhfhURXpgkSLGD1.pdf','2023-08-16 12:16:09',NULL),(41,'ASSET-DOCUMENT-IMAGES','lJlaDmFgAhBe2fE491dirWJWhSQ38Z4SzejIIpmK.png',13,'/storage/assetsUploads/lJlaDmFgAhBe2fE491dirWJWhSQ38Z4SzejIIpmK.png','http://127.0.0.1:8000/storage/assetsUploads/lJlaDmFgAhBe2fE491dirWJWhSQ38Z4SzejIIpmK.png','2023-08-16 12:16:09',NULL),(42,'ASSET-DOCUMENT-IMAGES','gdxwj06zZDIVdH2xP1hI5XNEsjMVsxtoMxOMMwd2.png',13,'/storage/assetsUploads/gdxwj06zZDIVdH2xP1hI5XNEsjMVsxtoMxOMMwd2.png','http://127.0.0.1:8000/storage/assetsUploads/gdxwj06zZDIVdH2xP1hI5XNEsjMVsxtoMxOMMwd2.png','2023-08-16 12:16:10',NULL);
/*!40000 ALTER TABLE `asset_attachments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `asset_valuation`
--

DROP TABLE IF EXISTS `asset_valuation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `asset_valuation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `asset_id` int DEFAULT NULL,
  `make` varchar(255) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  `registration` varchar(255) DEFAULT NULL,
  `frame_no` varchar(255) DEFAULT NULL,
  `engine_no` varchar(255) DEFAULT NULL,
  `year_manufacture` int DEFAULT NULL,
  `millage` int DEFAULT NULL,
  `en_leakage_comment` text,
  `en_noise_comment` text,
  `en_smoke_comment` text,
  `en_power_performance_comment` text,
  `en_other_comment` text,
  `en_rating` int DEFAULT NULL,
  `ch_frame_comment` text,
  `ch_rating` int DEFAULT NULL,
  `b_tank_com` text,
  `b_seat_com` text,
  `b_front_rim_tyre_com` text,
  `b_back_rim_tyre_com` text,
  `b_covers_com` text,
  `b_breaks_com` text,
  `b_rear_shocks_com` text,
  `b_front_shocks_com` text,
  `b_foot_rest_com` text,
  `b_crash_guard_com` text,
  `b_cables_com` text,
  `b_dashboard_com` text,
  `b_carrier_com` text,
  `b_stands_com` text,
  `b_fenders_com` text,
  `b_kick_start_com` text,
  `b_steering_com` text,
  `b_exhaust_com` text,
  `b_gear_levers_com` text,
  `b_levers_com` text,
  `b_mirrors_com` text,
  `b_body_final_com` text,
  `b_body_rating` int DEFAULT NULL,
  `e_battery_status_com` text,
  `e_horn_com` text,
  `e_lights_com` text,
  `e_starter_com` text,
  `e_signals_com` text,
  `e_charging_system_com` text,
  `e_electric_final_com` text,
  `e_electric_rating` int DEFAULT NULL,
  `general_com` text,
  `general_rating` int DEFAULT NULL,
  `tracker_imei` text,
  `tracker_sim_number` text,
  `asset_total_value` double DEFAULT NULL,
  `valuer_name` varchar(255) DEFAULT NULL,
  `valuer_signature` text,
  `supervisor_name` varchar(255) DEFAULT NULL,
  `supervisor_signature` text,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `asset_valuation`
--

LOCK TABLES `asset_valuation` WRITE;
/*!40000 ALTER TABLE `asset_valuation` DISABLE KEYS */;
INSERT INTO `asset_valuation` VALUES (4,123,456,'Sample Make','Sample Model','Blue','AB123CD','Frame123','Engine456',2022,5000,'No leakage','Slight noise','No smoke','Good performance','N/A',10,'Frame in good condition',10,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'General comments',10,'123456789012345','987654321098765',15000,'John Doe','John\'s Signature','Jane Smith','Jane\'s Signature','2023-08-16 12:41:56');
/*!40000 ALTER TABLE `asset_valuation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `at_config`
--

DROP TABLE IF EXISTS `at_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `at_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_id` int NOT NULL,
  `auth_username` varchar(50) DEFAULT NULL,
  `account_name` varchar(100) DEFAULT NULL,
  `api_key` varchar(200) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `at_config`
--

LOCK TABLES `at_config` WRITE;
/*!40000 ALTER TABLE `at_config` DISABLE KEYS */;
INSERT INTO `at_config` VALUES (1,1,'Magnifique','MagiqueLTD','0651dc07368b0052f301853a4be677903d8f4b7420cdaff4e1ffb6ff36090100','2023-07-21 11:52:10');
/*!40000 ALTER TABLE `at_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `client_assets`
--

DROP TABLE IF EXISTS `client_assets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `client_assets` (
  `id` int NOT NULL AUTO_INCREMENT,
  `brand` varchar(255) NOT NULL,
  `make` varchar(255) NOT NULL,
  `model` varchar(255) NOT NULL,
  `l_plate` varchar(255) NOT NULL,
  `chassis` varchar(255) NOT NULL,
  `odometer` varchar(255) NOT NULL,
  `a_condition` varchar(255) NOT NULL,
  `user_id` int NOT NULL,
  `eval_status` int NOT NULL DEFAULT '0',
  `eval_assigned_to` int NOT NULL DEFAULT '0',
  `eval_req_date` datetime DEFAULT NULL,
  `eval_comp_date` datetime DEFAULT NULL,
  `created_by` int NOT NULL,
  `created_at` datetime NOT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client_assets`
--

LOCK TABLES `client_assets` WRITE;
/*!40000 ALTER TABLE `client_assets` DISABLE KEYS */;
INSERT INTO `client_assets` VALUES (13,'SkyGo','Houjang','CRV123','KMEQ1554H','432556KJA','2580','excellent',16,1,8,'2023-08-16 12:25:00','2023-08-16 15:43:16',2,'2023-08-16 12:16:08',NULL);
/*!40000 ALTER TABLE `client_assets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `client_attachments`
--

DROP TABLE IF EXISTS `client_attachments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `client_attachments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `doc_type` varchar(255) NOT NULL,
  `doc_name` varchar(255) NOT NULL,
  `user_id` int NOT NULL,
  `doc_path` varchar(255) NOT NULL,
  `public_url` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `client_attachments`
--

LOCK TABLES `client_attachments` WRITE;
/*!40000 ALTER TABLE `client_attachments` DISABLE KEYS */;
INSERT INTO `client_attachments` VALUES (45,'NATIONAL-ID','JekPBKuDvnZn7eFYwRW0F7At5Ehh3ocjSTwzRBpS.pdf',16,'/storage/nationalIDUploads/JekPBKuDvnZn7eFYwRW0F7At5Ehh3ocjSTwzRBpS.pdf','http://127.0.0.1:8000/storage/nationalIDUploads/JekPBKuDvnZn7eFYwRW0F7At5Ehh3ocjSTwzRBpS.pdf','2023-08-16 12:13:40',NULL),(46,'PASSPORT-PICTURE','kp6CeOdAFgxLAbfMyBWLsUC3Xaxowg01o9o7cckx.webp',16,'/storage/passportPictureUploads/kp6CeOdAFgxLAbfMyBWLsUC3Xaxowg01o9o7cckx.webp','http://127.0.0.1:8000/storage/passportPictureUploads/kp6CeOdAFgxLAbfMyBWLsUC3Xaxowg01o9o7cckx.webp','2023-08-16 12:14:06',NULL),(47,'KRA-PIN','vjXHBznSzSE9ECjJ1QMi4wo1wzvH9pdM8vbnudw6.pdf',16,'/storage/kraPINUploads/vjXHBznSzSE9ECjJ1QMi4wo1wzvH9pdM8vbnudw6.pdf','http://127.0.0.1:8000/storage/kraPINUploads/vjXHBznSzSE9ECjJ1QMi4wo1wzvH9pdM8vbnudw6.pdf','2023-08-16 12:14:33',NULL),(48,'DRIVING-LICENSE','lNEqWiiACONSK4ns2H1MKOnWWB7UwiCtb1HqK5YX.pdf',16,'/storage/drivingLicenseUploads/lNEqWiiACONSK4ns2H1MKOnWWB7UwiCtb1HqK5YX.pdf','http://127.0.0.1:8000/storage/drivingLicenseUploads/lNEqWiiACONSK4ns2H1MKOnWWB7UwiCtb1HqK5YX.pdf','2023-08-16 12:15:00',NULL),(49,'LOAN_AGREEMENT','eXRcBO1VeKLvJ1uMoK5BiP2fdt15ctiE4Q81FkPj.pdf',16,'/storage/loanAgreements/eXRcBO1VeKLvJ1uMoK5BiP2fdt15ctiE4Q81FkPj.pdf','http://127.0.0.1:8000/storage/loanAgreements/eXRcBO1VeKLvJ1uMoK5BiP2fdt15ctiE4Q81FkPj.pdf','2023-08-16 12:44:27',NULL);
/*!40000 ALTER TABLE `client_attachments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `groups`
--

DROP TABLE IF EXISTS `groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `groups` (
  `id` int NOT NULL AUTO_INCREMENT,
  `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `group_ward` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `group_zone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `group_stage` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `chair_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `chair_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `tres_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `tres_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `sec_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `sec_phone` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `groups`
--

LOCK TABLES `groups` WRITE;
/*!40000 ALTER TABLE `groups` DISABLE KEYS */;
INSERT INTO `groups` VALUES (2,'Tumaini','Kayole','Kayole East','Super Riders','Austine','12345678','Magnifique','32145678','Limo','98765432','2023-08-16 12:11:27',NULL);
/*!40000 ALTER TABLE `groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loans`
--

DROP TABLE IF EXISTS `loans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loans` (
  `id` int NOT NULL AUTO_INCREMENT,
  `userID` int DEFAULT NULL,
  `asset_id` int DEFAULT NULL,
  `loanPrincipal` double DEFAULT NULL,
  `interestPercentage` double DEFAULT NULL,
  `loanPurpose` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `guarantorID_one` int DEFAULT NULL,
  `guarantorID_two` int DEFAULT NULL,
  `paymentDate` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `loanStatus` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `loanAccountMPesa` varchar(250) COLLATE utf8mb4_general_ci NOT NULL,
  `agreement_attachment_id` int DEFAULT NULL,
  `loanStatusLevelOne` int NOT NULL DEFAULT '0',
  `loanStatusLevelTwo` int NOT NULL DEFAULT '0',
  `l_one_updated_at` datetime DEFAULT NULL,
  `l_two_updated_at` datetime DEFAULT NULL,
  `disburse_initiated` tinyint(1) DEFAULT NULL,
  `disbursed_at` datetime DEFAULT NULL,
  `createdAt` datetime DEFAULT NULL,
  `updatedAt` datetime DEFAULT NULL,
  `l_one_update_comment` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `l_two_update_comment` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `loan_term` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `loanAccountMPesa` (`loanAccountMPesa`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loans`
--

LOCK TABLES `loans` WRITE;
/*!40000 ALTER TABLE `loans` DISABLE KEYS */;
INSERT INTO `loans` VALUES (13,16,13,100,10,'Personal',797301935,797301935,'2023-09-15','APPROVED','D92756431H',49,1,1,'2023-08-16 12:45:27','2023-08-16 12:45:52',1,'2023-08-16 12:46:01','2023-08-16 12:44:28','2023-08-16 12:44:28','Confirmed','Confirmed',30);
/*!40000 ALTER TABLE `loans` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mpesa_apps`
--

DROP TABLE IF EXISTS `mpesa_apps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mpesa_apps` (
  `id` int NOT NULL AUTO_INCREMENT,
  `consumer_key` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `consumer_secret` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `app_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `api_key` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `shotcode` int NOT NULL,
  `products_activated` int NOT NULL,
  `response_type` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `c2b_confirmation_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `c2b_validation_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `confirmation_url` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `validation_url` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `transaction_type` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `is_b2c_enabled` tinyint(1) DEFAULT NULL,
  `b2c_shortcode` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `b2c_initiator` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `b2c_password` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `b2c_callback_url` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `b2c_queue_timeout_url` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `b2c_consumer_key` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `b2c_consumer_secret` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mpesa_apps`
--

LOCK TABLES `mpesa_apps` WRITE;
/*!40000 ALTER TABLE `mpesa_apps` DISABLE KEYS */;
INSERT INTO `mpesa_apps` VALUES (1,'pzEA1gmSuxh5g43F8pOhxhh9HAv2UfIF','WJJs92PgbAx85Tbp','MAGIQUE_TECH','fbf39986836f89e221fa82ae90dac34e4c43a780680ac69ef3ef598b10a73273',7213149,1,'Complete','https://fintech.tequelabstechnologies.tech/payments/momo/postc2b/1','https://fintech.tequelabstechnologies.tech/payments/momo/postc2b/1','https://fintech.tequelabstechnologies.tech/payments/momo/poststkpush/1','https://fintech.tequelabstechnologies.tech/payments/momo/poststkpush/1','2023-07-16 22:47:43','2023-07-16 22:47:43','CustomerPayBillOnline',1,'3028669','Austine','yV6soirkjWXqwboOCYJibg==','https://fintech.tequelabstechnologies.tech/payments/momo/postb2c/1','https://fintech.tequelabstechnologies.tech/payments/momo/b2c_timeout/1','AtRvk7LgGiQbRbHZqwGAPEctuaZkvoVX','nqWb8VARYkl8thQs');
/*!40000 ALTER TABLE `mpesa_apps` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mpesa_b2c`
--

DROP TABLE IF EXISTS `mpesa_b2c`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mpesa_b2c` (
  `id` int NOT NULL AUTO_INCREMENT,
  `command_id` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `party_b` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `remarks` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `occasion` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `response_code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `response_description` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `conversation_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `originator_conversation_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `reference_Data` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `result_desc` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `result_type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `result_code` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `transaction_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `transaction_receipt` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `result_parameters` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `transaction_amount` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `reciever_name` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `b2c_working_account_available_funds` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `b2c_utility_account_available_funds` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `transaction_completed_datetime` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `receiver_party_public_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `b2c_charges_paid_account_available_funds` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `b2c_recipient_is_registered_customer` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `app_id` int NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mpesa_b2c`
--

LOCK TABLES `mpesa_b2c` WRITE;
/*!40000 ALTER TABLE `mpesa_b2c` DISABLE KEYS */;
INSERT INTO `mpesa_b2c` VALUES (41,'BusinessPayment','254797301935','Loan Disbursement','D92756431H','0','Accept the service request successfully.','AG_20230816_20205b4c72807e7c80e6','11450-57438022-1','ReferenceData(referenceItem=ReferenceItem(key=QueueTimeoutURL, value=http://internalapi.safaricom.co.ke/mpesa/b2cresults/v1/submit))','The service request is processed successfully.','0','0','RHG79QEDHX','RHG79QEDHX',NULL,'100','254797301935 - MAGNIFIQUE NSENGIMANA','10000.00','113.00','16.08.2023 15:45:57','254797301935 - MAGNIFIQUE NSENGIMANA','Y','Y',1,'2023-08-16 12:45:57','2023-08-16 12:46:01');
/*!40000 ALTER TABLE `mpesa_b2c` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mpesa_c2b`
--

DROP TABLE IF EXISTS `mpesa_c2b`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mpesa_c2b` (
  `num` int NOT NULL AUTO_INCREMENT,
  `TransactionType` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `TransID` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `TransTime` datetime DEFAULT NULL,
  `TransAmount` decimal(18,4) DEFAULT NULL,
  `BusinessShortCode` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `BillRefNumber` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ManualRefNumber` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `InvoiceNumber` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `OrgAccountBalance` decimal(18,4) DEFAULT NULL,
  `ThirdPartyTransID` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `MSISDN` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `FirstName` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `MiddleName` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `LastName` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `appId` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`num`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mpesa_c2b`
--

LOCK TABLES `mpesa_c2b` WRITE;
/*!40000 ALTER TABLE `mpesa_c2b` DISABLE KEYS */;
INSERT INTO `mpesa_c2b` VALUES (37,'Pay Bill','RHG99R8QRR','2023-08-16 15:54:17',1.0000,'7213149','D92756431H',NULL,'',142.0000,'','254718728894','AUSTINE','OCHIENG','GWA','1','2023-08-16 12:54:25','2023-08-16 12:54:25'),(38,'Pay Bill','RHG69RBUG8','2023-08-16 15:55:16',21.0000,'7213149','D92756431H',NULL,'',163.0000,'','254797301935','MAGNIFIQUE','NSENGIMANA','','1','2023-08-16 12:55:17','2023-08-16 12:55:17');
/*!40000 ALTER TABLE `mpesa_c2b` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mpesa_stk`
--

DROP TABLE IF EXISTS `mpesa_stk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mpesa_stk` (
  `id` int NOT NULL AUTO_INCREMENT,
  `MerchantRequestID` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `CheckoutRequestID` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ResponseCode` int DEFAULT NULL,
  `ResponseDescription` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `CustomerMessage` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ResultCode` int DEFAULT NULL,
  `ResultDesc` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `RequestID` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ErrorCode` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `ErrorMessage` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `Amount` decimal(10,2) DEFAULT NULL,
  `org_balance` double DEFAULT NULL,
  `MpesaReceiptNumber` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `TransactionDate` datetime DEFAULT NULL,
  `PhoneNumber` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `AppID` int NOT NULL,
  `PaymentFor` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `UserID` int DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mpesa_stk`
--

LOCK TABLES `mpesa_stk` WRITE;
/*!40000 ALTER TABLE `mpesa_stk` DISABLE KEYS */;
/*!40000 ALTER TABLE `mpesa_stk` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_deposit`
--

DROP TABLE IF EXISTS `request_deposit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `request_deposit` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `wallet_account_number` varchar(100) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `approval_status` tinyint(1) DEFAULT NULL,
  `approved_by` int DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_completed` tinyint(1) DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_deposit`
--

LOCK TABLES `request_deposit` WRITE;
/*!40000 ALTER TABLE `request_deposit` DISABLE KEYS */;
/*!40000 ALTER TABLE `request_deposit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `request_withdraw`
--

DROP TABLE IF EXISTS `request_withdraw`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `request_withdraw` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `wallet_account_number` varchar(100) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `approval_status` tinyint(1) DEFAULT NULL,
  `approved_by` int DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `is_completed` tinyint(1) DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `request_withdraw`
--

LOCK TABLES `request_withdraw` WRITE;
/*!40000 ALTER TABLE `request_withdraw` DISABLE KEYS */;
/*!40000 ALTER TABLE `request_withdraw` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` int NOT NULL,
  `usertype` varchar(50) NOT NULL,
  `roles` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`,`usertype`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (2,'Admin',NULL),(8,'Evaluator',NULL),(16,'Client',NULL);
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_wallet`
--

DROP TABLE IF EXISTS `user_wallet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_wallet` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `wallet_account_number` varchar(100) DEFAULT NULL,
  `wallet_balance` double DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `last_updated_at` datetime DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_wallet`
--

LOCK TABLES `user_wallet` WRITE;
/*!40000 ALTER TABLE `user_wallet` DISABLE KEYS */;
INSERT INTO `user_wallet` VALUES (4,4,NULL,0,'2023-07-25 09:54:04','2023-07-25 09:54:04',NULL),(5,1,NULL,0,'2023-07-25 09:54:04','2023-07-25 09:54:04',NULL),(6,2,NULL,0,'2023-07-25 09:54:04','2023-07-25 09:54:04',NULL),(7,12,NULL,0,'2023-07-25 09:54:04','2023-07-25 09:54:04',NULL),(8,11,NULL,0,'2023-07-25 09:54:04','2023-07-25 09:54:04',NULL),(9,10,NULL,0,'2023-07-25 09:54:04','2023-07-25 09:54:04',NULL),(10,13,NULL,0,'2023-07-25 09:54:04','2023-07-25 09:54:04',NULL),(11,8,NULL,0,'2023-07-25 09:54:04','2023-07-25 09:54:04',NULL),(12,3,NULL,0,'2023-07-25 09:54:04','2023-07-25 09:54:04',NULL),(13,9,NULL,0,'2023-07-25 09:54:04','2023-07-25 09:54:04',NULL);
/*!40000 ALTER TABLE `user_wallet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `middle_name` varchar(250) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `last_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `phone` varchar(15) COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(250) COLLATE utf8mb4_general_ci NOT NULL,
  `nationalId` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `user_status` int NOT NULL DEFAULT '1',
  `created_by` int NOT NULL,
  `group_id` int DEFAULT NULL,
  `app_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `users_pk` (`phone`),
  UNIQUE KEY `users_pk2` (`nationalId`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (2,'Magnifique',NULL,'Nsengimana','mtech@gmail.com','254797495885','$2y$10$V9xr4nb.WQb0Ew3IceNhJ.HQxU4AMSSntqB1fkb3KpGgm7xtz4oG2','12345678','2023-08-16 14:58:48',1,2,NULL,1),(8,'OSANO','','GARSON','osanogarson@jiwezeshe.com','254797301934','$2y$11$VSzm1.9CyCyKOaf3tpIGXObxipNqyT4Eor5BOh4.FzsZlMi5tXbWa','101231456','2023-07-21 09:15:45',1,2,NULL,1),(16,'MAGNIFIQUE','','NSENGIMANA','magnifiquensengimana@jiwezeshe.com','254797301935','$2y$11$7VigV4MnORxbV8Sa4.Fak.jN0UL6Xp9LCxfXoR2QbuhfD3259lFE.','10123144','2023-08-16 12:12:20',1,2,2,1);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wallet_transaction_types`
--

DROP TABLE IF EXISTS `wallet_transaction_types`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wallet_transaction_types` (
  `id` int NOT NULL AUTO_INCREMENT,
  `transaction_type` varchar(50) DEFAULT NULL,
  `transaction_description` varchar(200) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wallet_transaction_types`
--

LOCK TABLES `wallet_transaction_types` WRITE;
/*!40000 ALTER TABLE `wallet_transaction_types` DISABLE KEYS */;
/*!40000 ALTER TABLE `wallet_transaction_types` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wallet_transactions`
--

DROP TABLE IF EXISTS `wallet_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wallet_transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `transaction_channel` varchar(50) DEFAULT NULL,
  `transaction_reference` varchar(100) DEFAULT NULL,
  `wallet_account_number` varchar(100) DEFAULT NULL,
  `transaction_amount` double DEFAULT NULL,
  `is_verified` tinyint(1) DEFAULT NULL,
  `verified_by` int DEFAULT NULL,
  `verified_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `deleted_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wallet_transactions`
--

LOCK TABLES `wallet_transactions` WRITE;
/*!40000 ALTER TABLE `wallet_transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `wallet_transactions` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-08-16 16:55:02
