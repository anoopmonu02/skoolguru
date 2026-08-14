-- =============================================================================
--  V1__baseline.sql - schema baseline captured from the live 'skooldb' database
--  on 2026-08-13, before the Spring Boot 4.1 upgrade.
--
--  This file is NOT executed against existing databases. application.properties
--  sets spring.flyway.baseline-on-migrate=true and baseline-version=1, so Flyway
--  stamps an existing schema as already at version 1 and starts applying from V2.
--
--  It IS executed on a fresh/empty database (new dev machine, CI, a new school's
--  install), which is the point: from now on the schema is described by files in
--  source control instead of being whatever ddl-auto=update happened to leave
--  behind.
--
--  Generated with:
--    mysqldump --no-data --routines --triggers --skip-comments --              --skip-add-drop-table --set-gtid-purged=OFF skooldb
--  AUTO_INCREMENT counters stripped (machine-specific).
-- =============================================================================

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
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `academic_students` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `board_sr_no` varchar(255) DEFAULT NULL,
  `class_sr_no` varchar(255) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `last_updated` datetime(6) DEFAULT NULL,
  `migration_date` datetime(6) DEFAULT NULL,
  `roll_no` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `uuid` binary(16) NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `grade_id` bigint NOT NULL,
  `medium_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `section_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `opening_balance` decimal(10,2) DEFAULT NULL,
  `opening_balance_remark` varchar(500) DEFAULT NULL,
  `is_mid_session_migration` bit(1) NOT NULL,
  `is_migrated` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_kte4iqwwh2rcgk9b5kww700cd` (`uuid`),
  KEY `FKadutrut3wcr12qewd2h896y6g` (`academic_year_id`),
  KEY `FKm9sjt4uq4stjueip8hfn3ssf1` (`grade_id`),
  KEY `FK61fd7dy0l6s9stwj2s6goin6k` (`medium_id`),
  KEY `FK6umd6c52lhkism39ngnf0quw3` (`school_id`),
  KEY `FKl93otowbqgr8bn8d593gtmpbh` (`section_id`),
  KEY `FKfgb86axd1s0ptn04s92s4sdl8` (`student_id`),
  KEY `FKeyua9xrue32sh6ph6fuonf57w` (`updated_by`),
  KEY `FKdh95jv8sdmdxvdtpr050cg3ev` (`created_by`),
  CONSTRAINT `FK61fd7dy0l6s9stwj2s6goin6k` FOREIGN KEY (`medium_id`) REFERENCES `medium` (`id`),
  CONSTRAINT `FK6umd6c52lhkism39ngnf0quw3` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKadutrut3wcr12qewd2h896y6g` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKdh95jv8sdmdxvdtpr050cg3ev` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKeyua9xrue32sh6ph6fuonf57w` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKfgb86axd1s0ptn04s92s4sdl8` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `FKl93otowbqgr8bn8d593gtmpbh` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`),
  CONSTRAINT `FKm9sjt4uq4stjueip8hfn3ssf1` FOREIGN KEY (`grade_id`) REFERENCES `grade` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `academic_year` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `end_date` date DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `session_format` varchar(50) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sessionformat` (`session_format`,`school_id`),
  KEY `FK7p7aivj6xdfa7hwq1ks7jenbe` (`school_id`),
  KEY `FKi337p01tplybonvl2oiurlar7` (`created_by`),
  KEY `FKpnpwchjeqxqxiriu5u1ag7ikg` (`updated_by`),
  CONSTRAINT `FK7p7aivj6xdfa7hwq1ks7jenbe` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKi337p01tplybonvl2oiurlar7` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKpnpwchjeqxqxiriu5u1ag7ikg` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_screen` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `module` varchar(255) NOT NULL,
  `screen_key` varchar(255) NOT NULL,
  `screen_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4wniv13244h402kyifhj9tt5f` (`screen_key`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attendance_date` datetime(6) DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `is_present` bit(1) NOT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `remark` text,
  `status` varchar(255) NOT NULL,
  `uuid` binary(16) NOT NULL,
  `academic_student_id` bigint NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `created_by` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_qhrh8cyfjjaticnch36snarbu` (`uuid`),
  KEY `idx_attendance_student` (`academic_student_id`),
  KEY `idx_attendance_date` (`attendance_date`),
  KEY `FKblenciu833fbglp0ro69wu7b6` (`academic_year_id`),
  KEY `FKg7dtnois2g6ghgu4bl5gym92u` (`created_by`),
  KEY `FKr4ms1d0xrggll9shckfiviee0` (`school_id`),
  KEY `FKgorx7ahfgws1b6fbes17qjga6` (`updated_by`),
  CONSTRAINT `FKblenciu833fbglp0ro69wu7b6` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKg7dtnois2g6ghgu4bl5gym92u` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKgorx7ahfgws1b6fbes17qjga6` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKj3iy9kccy4yofdyc6tp0djnsx` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`),
  CONSTRAINT `FKr4ms1d0xrggll9shckfiviee0` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attendance_confirmation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `school_id` bigint NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `attendance_date` date NOT NULL,
  `is_confirmed` tinyint(1) NOT NULL DEFAULT '1',
  `created_by` bigint NOT NULL,
  `creation_date` datetime NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `last_updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_attendance_confirmation_day` (`school_id`,`academic_year_id`,`attendance_date`),
  KEY `fk_attendance_confirmation_academic_year` (`academic_year_id`),
  KEY `fk_attendance_confirmation_created_by` (`created_by`),
  KEY `fk_attendance_confirmation_updated_by` (`updated_by`),
  CONSTRAINT `fk_attendance_confirmation_academic_year` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `fk_attendance_confirmation_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_attendance_confirmation_school` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `fk_attendance_confirmation_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bank` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bank_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_k7b1fp39xemdvdtco9j7uk62y` (`bank_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bank_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `academic_student_id` bigint NOT NULL,
  `changed_by` bigint NOT NULL,
  `old_bank_id` bigint DEFAULT NULL,
  `new_bank_id` bigint DEFAULT NULL,
  `old_account_no` varchar(255) DEFAULT NULL,
  `new_account_no` varchar(255) DEFAULT NULL,
  `old_branch_name` varchar(255) DEFAULT NULL,
  `new_branch_name` varchar(255) DEFAULT NULL,
  `old_ifsc_code` varchar(255) DEFAULT NULL,
  `new_ifsc_code` varchar(255) DEFAULT NULL,
  `changed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_bcl_changed_by` (`changed_by`),
  KEY `fk_bcl_old_bank` (`old_bank_id`),
  KEY `fk_bcl_new_bank` (`new_bank_id`),
  KEY `idx_bcl_academic_student` (`academic_student_id`,`changed_at`),
  CONSTRAINT `fk_bcl_academic_student` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`),
  CONSTRAINT `fk_bcl_changed_by` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_bcl_new_bank` FOREIGN KEY (`new_bank_id`) REFERENCES `bank` (`id`),
  CONSTRAINT `fk_bcl_old_bank` FOREIGN KEY (`old_bank_id`) REFERENCES `bank` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cast` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cast_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_btat26a9dc0ep1hwlbewyd8mn` (`cast_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_lroeo5fvfdeg4hpicn4lw7x9b` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `city` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `city_name` varchar(100) DEFAULT NULL,
  `province_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKll21eddgtrjc9f40ueeouyr8f` (`province_id`),
  CONSTRAINT `FKll21eddgtrjc9f40ueeouyr8f` FOREIGN KEY (`province_id`) REFERENCES `province` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` text,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `email` varchar(255) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `mobile1` varchar(10) DEFAULT NULL,
  `mobile2` varchar(10) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `pic` varchar(255) DEFAULT NULL,
  `pincode` varchar(6) DEFAULT NULL,
  `registration_date` datetime(6) DEFAULT NULL,
  `registration_no` varchar(255) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `city_id` bigint NOT NULL,
  `province_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_crkjmjk1oj8gb6j6t5kt7gcxm` (`name`),
  KEY `FKt79b5wvqbf38jtkjx36vp9vam` (`city_id`),
  KEY `FK95nyta7b1ns77s1873t6655vo` (`province_id`),
  KEY `FK5fvme5alfjk2i4e4q4n7996ag` (`updated_by`),
  KEY `FKj5e49hu5k5m8moub9qwh49baa` (`created_by`),
  CONSTRAINT `FK5fvme5alfjk2i4e4q4n7996ag` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FK95nyta7b1ns77s1873t6655vo` FOREIGN KEY (`province_id`) REFERENCES `province` (`id`),
  CONSTRAINT `FKj5e49hu5k5m8moub9qwh49baa` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKt79b5wvqbf38jtkjx36vp9vam` FOREIGN KEY (`city_id`) REFERENCES `city` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `discount_class_map` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(12,2) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `last_updated` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `discounthead_id` bigint NOT NULL,
  `grade_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_discountclassmap` (`grade_id`,`discounthead_id`,`academic_year_id`,`school_id`),
  KEY `FK7xt6wjcbdn1b2o9r5hb78skgw` (`academic_year_id`),
  KEY `FKdksv99u2ixqc5u68yemybl29s` (`discounthead_id`),
  KEY `FKhbvw1jb1jernmj6jrl4w9q0sk` (`school_id`),
  KEY `FKkhsywt3trq88oqtjfh8x0in3m` (`updated_by`),
  KEY `FKg0g8eihhd5ghmlai4kawcr1sf` (`created_by`),
  CONSTRAINT `FK7xt6wjcbdn1b2o9r5hb78skgw` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKdksv99u2ixqc5u68yemybl29s` FOREIGN KEY (`discounthead_id`) REFERENCES `discounthead` (`id`),
  CONSTRAINT `FKg0g8eihhd5ghmlai4kawcr1sf` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKhbvw1jb1jernmj6jrl4w9q0sk` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKkhsywt3trq88oqtjfh8x0in3m` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKo7gtx4988oaqyy1viohpu8l6l` FOREIGN KEY (`grade_id`) REFERENCES `grade` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `discount_month_map` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `is_applicable` bit(1) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `discounthead_id` bigint NOT NULL,
  `month_master_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_discountmonthmap` (`month_master_id`,`discounthead_id`,`academic_year_id`,`school_id`),
  KEY `FKqm52kob2soug44aeyffvyla5n` (`academic_year_id`),
  KEY `FKobc1d83iv953s7gkfpx0l1mdj` (`discounthead_id`),
  KEY `FKhuvgbtfc534dl3h1lv0iq1mnm` (`school_id`),
  KEY `FK39u9o3yshvkmshxxysy9obo2x` (`updated_by`),
  KEY `FKrrpm18cxr6lpcd6axyu7747x4` (`created_by`),
  CONSTRAINT `FK39u9o3yshvkmshxxysy9obo2x` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKfluhsa9rmw9c6kl68ae5s79xy` FOREIGN KEY (`month_master_id`) REFERENCES `month_master` (`id`),
  CONSTRAINT `FKhuvgbtfc534dl3h1lv0iq1mnm` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKobc1d83iv953s7gkfpx0l1mdj` FOREIGN KEY (`discounthead_id`) REFERENCES `discounthead` (`id`),
  CONSTRAINT `FKqm52kob2soug44aeyffvyla5n` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKrrpm18cxr6lpcd6axyu7747x4` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `discounthead` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `discount_name` varchar(100) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_d2u5okaujq9u620dynltvyn0f` (`discount_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_by` bigint NOT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `department` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `designation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `employee_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `employee_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `father_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `joining_date` datetime(6) DEFAULT NULL,
  `landmark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `mobile1` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile2` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mother_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nationality` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `uuid` binary(16) NOT NULL,
  `school_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_etqhw9qqnad1kyjq3ks1glw8x` (`employee_code`),
  UNIQUE KEY `UK_h62keojjiby719f0ajqpxy1b9` (`uuid`),
  UNIQUE KEY `UK_j2dmgsma6pont6kf7nic9elpd` (`user_id`),
  KEY `FKn730f0yj566lb40x5emvy1d30` (`school_id`),
  KEY `FKf5a2r351hvnkfxbrm60givndb` (`created_by`),
  KEY `FK2fgi564oek1g2lfku3v6fsfm5` (`updated_by`),
  CONSTRAINT `FK2fgi564oek1g2lfku3v6fsfm5` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FK69x3vjuy1t5p18a5llb8h2fjx` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKf5a2r351hvnkfxbrm60givndb` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKn730f0yj566lb40x5emvy1d30` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_details` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creation_date` datetime(6) DEFAULT NULL,
  `exam_declared_date` datetime(6) NOT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `remarks` text,
  `uuid` binary(16) NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `examination_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_examdetails` (`examination_id`,`academic_year_id`,`school_id`),
  UNIQUE KEY `UK_ejxx18cbexjekrprgy4b2ur8q` (`uuid`),
  KEY `FK4mm2gmcx8e86gwud949vq9lb4` (`academic_year_id`),
  KEY `FKagvlalewu3uly4vrplgqw3unx` (`school_id`),
  CONSTRAINT `FK4mm2gmcx8e86gwud949vq9lb4` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKagvlalewu3uly4vrplgqw3unx` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKh9s3emh4w5jgbjidh1876qie4` FOREIGN KEY (`examination_id`) REFERENCES `examination` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_result_correction_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `batch_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `change_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `corrected_at` datetime(6) DEFAULT NULL,
  `new_division` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `new_obtained_marks` bigint DEFAULT NULL,
  `new_percentage_marks` double DEFAULT NULL,
  `new_result` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `new_total_marks` bigint DEFAULT NULL,
  `old_division` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `old_obtained_marks` bigint DEFAULT NULL,
  `old_percentage_marks` double DEFAULT NULL,
  `old_result` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `old_total_marks` bigint DEFAULT NULL,
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `corrected_by` bigint NOT NULL,
  `exam_result_summary_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6352snp3b0heqrlwk73xfufa2` (`corrected_by`),
  KEY `FKk98e5krjrh65hme9qlsr4ee6o` (`exam_result_summary_id`),
  CONSTRAINT `FK6352snp3b0heqrlwk73xfufa2` FOREIGN KEY (`corrected_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKk98e5krjrh65hme9qlsr4ee6o` FOREIGN KEY (`exam_result_summary_id`) REFERENCES `exam_result_summary` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_result_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` bigint NOT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `division` varchar(255) DEFAULT NULL,
  `exam_result_date` datetime(6) NOT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `obtained_marks` bigint DEFAULT NULL,
  `percentage_marks` double DEFAULT NULL,
  `remarks` text,
  `result` varchar(255) DEFAULT NULL,
  `total_marks` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `uuid` binary(16) NOT NULL,
  `academic_student_id` bigint NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `exam_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_dferafkhy2fxhkd4lfcenu14q` (`uuid`),
  KEY `FKnoetynbqpkriccv986j7ldij4` (`academic_student_id`),
  KEY `FKl0vckj7l6csx6ma9wdtsf54rn` (`academic_year_id`),
  KEY `FKp2riput4g4wkd4smnja2727gd` (`exam_id`),
  KEY `FKojn3qwecjhgbnjtyysqsqdeap` (`school_id`),
  KEY `FKc5qqyn3raqfpwmfvhi3ft1aok` (`created_by`),
  KEY `FKnrpk16nxxsdb0exr74p9prc30` (`updated_by`),
  CONSTRAINT `FKc5qqyn3raqfpwmfvhi3ft1aok` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKl0vckj7l6csx6ma9wdtsf54rn` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKnoetynbqpkriccv986j7ldij4` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`),
  CONSTRAINT `FKnrpk16nxxsdb0exr74p9prc30` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKojn3qwecjhgbnjtyysqsqdeap` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKp2riput4g4wkd4smnja2727gd` FOREIGN KEY (`exam_id`) REFERENCES `exam_details` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `examination` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `examination_name` varchar(255) NOT NULL,
  `remarks` text,
  `uuid` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_bvsjk89cgiwa0p4gqkxe0wv92` (`examination_name`),
  UNIQUE KEY `UK_f3t4d78hv7remilgmh597067x` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `family_accounts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `mobile` varchar(10) NOT NULL,
  `must_change_password` bit(1) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `status` varchar(20) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlu20po0n9kslhh24poi7vcv51` (`mobile`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fcm_device_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `academic_student_id` bigint NOT NULL,
  `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_fdt_token_student` (`token`,`academic_student_id`),
  KEY `idx_fdt_student` (`academic_student_id`),
  KEY `idx_fdt_token` (`token`),
  CONSTRAINT `fk_fcm_device_tokens_academic_student` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee_class_map` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(12,2) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `last_updated` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `feehead_id` bigint NOT NULL,
  `grade_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feeclassmap` (`grade_id`,`feehead_id`,`academic_year_id`,`school_id`),
  KEY `FKpsg4418rq21ta2ch0ba1jc1uj` (`academic_year_id`),
  KEY `FKf2nt1agn5m3am94cl4y5efp92` (`feehead_id`),
  KEY `FKd0x647552x0rvie43h78ha8ow` (`school_id`),
  KEY `FK1kdbortdvnq24usvjj63awmyu` (`updated_by`),
  KEY `FKcwivmy188n20gyscqik8p75b3` (`created_by`),
  CONSTRAINT `FK1kdbortdvnq24usvjj63awmyu` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKcwivmy188n20gyscqik8p75b3` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKd0x647552x0rvie43h78ha8ow` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKf2nt1agn5m3am94cl4y5efp92` FOREIGN KEY (`feehead_id`) REFERENCES `feehead` (`id`),
  CONSTRAINT `FKk0c4x7anaqg9ck5ie4cja7y32` FOREIGN KEY (`grade_id`) REFERENCES `grade` (`id`),
  CONSTRAINT `FKpsg4418rq21ta2ch0ba1jc1uj` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee_date` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `fee_submissiondate` datetime(6) NOT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `month_master_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feedate` (`month_master_id`,`academic_year_id`,`school_id`),
  KEY `FK93xrmxnasbcgtkect4la8t97n` (`academic_year_id`),
  KEY `FKsffl04d72234nb5gxequb0aox` (`school_id`),
  KEY `FKi7fp9dk9279yh7rsre637n28g` (`updated_by`),
  KEY `FK1cd5yfihe77x3d4edp62vfe83` (`created_by`),
  CONSTRAINT `FK1cd5yfihe77x3d4edp62vfe83` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FK93xrmxnasbcgtkect4la8t97n` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKdywtrc2oyyuo776fexarkeeum` FOREIGN KEY (`month_master_id`) REFERENCES `month_master` (`id`),
  CONSTRAINT `FKi7fp9dk9279yh7rsre637n28g` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKsffl04d72234nb5gxequb0aox` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee_month_map` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `is_applicable` bit(1) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `feehead_id` bigint NOT NULL,
  `month_master_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feemonthmap` (`month_master_id`,`feehead_id`,`academic_year_id`,`school_id`),
  KEY `FKch0q0a9wp1ecbf50ginfppck3` (`academic_year_id`),
  KEY `FKm48jaxc8wgdtocws9lpeuilwf` (`feehead_id`),
  KEY `FKveamq64iaop6s4rojlh0wn2l` (`school_id`),
  KEY `FKofmqe0gaiy0k4tpuo8q6keqjs` (`updated_by`),
  KEY `FKgqich3s4nhb203mxqkw6krfx7` (`created_by`),
  CONSTRAINT `FKch0q0a9wp1ecbf50ginfppck3` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKgqich3s4nhb203mxqkw6krfx7` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKm48jaxc8wgdtocws9lpeuilwf` FOREIGN KEY (`feehead_id`) REFERENCES `feehead` (`id`),
  CONSTRAINT `FKn6wsqut43jv3txqrciq98jjhj` FOREIGN KEY (`month_master_id`) REFERENCES `month_master` (`id`),
  CONSTRAINT `FKofmqe0gaiy0k4tpuo8q6keqjs` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKveamq64iaop6s4rojlh0wn2l` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee_submission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `balance_amount` decimal(12,2) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `discount_amount` decimal(12,2) DEFAULT NULL,
  `fee_remark` text,
  `fee_submission_date` datetime(6) DEFAULT NULL,
  `fine_amount` decimal(12,2) DEFAULT NULL,
  `fine_remark` varchar(255) DEFAULT NULL,
  `full_payment_amount` decimal(12,2) DEFAULT NULL,
  `full_payment_remark` varchar(255) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `paid_amount` decimal(12,2) DEFAULT NULL,
  `receipt_no` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_amount` decimal(12,2) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `academic_student_id` bigint NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `discounthead_id` bigint DEFAULT NULL,
  `school_id` bigint NOT NULL,
  `payment_type` varchar(255) DEFAULT NULL,
  `previous_fee_balance_remark` varchar(255) DEFAULT NULL,
  `migration_discount_amount` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhn3gqq5mfg1mwtky3gg10vesw` (`academic_student_id`),
  KEY `FK9p9ijrg3dpkqvehjgxetrn5fd` (`academic_year_id`),
  KEY `FKdjm49ogn12nfh2pu293kblaes` (`discounthead_id`),
  KEY `FKggichowt8k3au5f7b524ep3fj` (`school_id`),
  KEY `FKj8yaejuh4yv6eubj6ook59htw` (`updated_by`),
  KEY `FKq7wegkjrc5oqtd78bft7mm60c` (`created_by`),
  CONSTRAINT `FK9p9ijrg3dpkqvehjgxetrn5fd` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKdjm49ogn12nfh2pu293kblaes` FOREIGN KEY (`discounthead_id`) REFERENCES `discounthead` (`id`),
  CONSTRAINT `FKggichowt8k3au5f7b524ep3fj` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKhn3gqq5mfg1mwtky3gg10vesw` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`),
  CONSTRAINT `FKj8yaejuh4yv6eubj6ook59htw` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKq7wegkjrc5oqtd78bft7mm60c` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee_submission_balance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `balance_amount` decimal(12,2) DEFAULT NULL,
  `fee_date` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `fee_submission_id` bigint DEFAULT NULL,
  `student_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_t81eta7dqf93qtoty2nthkg25` (`fee_submission_id`),
  KEY `FKnfvfopmhyu7svtc2kjw6j9n1d` (`student_id`),
  CONSTRAINT `FKaulcyqgnsijqayexi0fefcdip` FOREIGN KEY (`fee_submission_id`) REFERENCES `fee_submission` (`id`),
  CONSTRAINT `FKnfvfopmhyu7svtc2kjw6j9n1d` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee_submission_months` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `status` varchar(255) DEFAULT NULL,
  `fee_submission_id` bigint DEFAULT NULL,
  `month_master_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfd1ip3fl880g5g3c1rxl6agkw` (`fee_submission_id`),
  KEY `FKpjxiu406erdrvw6drb1tucaff` (`month_master_id`),
  CONSTRAINT `FKfd1ip3fl880g5g3c1rxl6agkw` FOREIGN KEY (`fee_submission_id`) REFERENCES `fee_submission` (`id`),
  CONSTRAINT `FKpjxiu406erdrvw6drb1tucaff` FOREIGN KEY (`month_master_id`) REFERENCES `month_master` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fee_submission_sub` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(12,2) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `fee_submission_id` bigint DEFAULT NULL,
  `fee_head_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl5wxs6ss3gbc0ibpvusln0hyr` (`fee_submission_id`),
  KEY `FKe7j40ximvd2cph9a4bxw6rf2t` (`fee_head_id`),
  CONSTRAINT `FKe7j40ximvd2cph9a4bxw6rf2t` FOREIGN KEY (`fee_head_id`) REFERENCES `feehead` (`id`),
  CONSTRAINT `FKl5wxs6ss3gbc0ibpvusln0hyr` FOREIGN KEY (`fee_submission_id`) REFERENCES `fee_submission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `feehead` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `fee_head_name` varchar(100) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_cx6hmemt44qkdqgg1moaak1wr` (`fee_head_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fine` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `fine_amount` int NOT NULL,
  `frequency` int NOT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `max_calculated` int NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `finehead_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fine_amount` (`finehead_id`,`academic_year_id`,`school_id`),
  KEY `FK9c50llje03tw2uaq3qoonvc7r` (`academic_year_id`),
  KEY `FK7xbvhqxg7nv6satydyb0au3t7` (`school_id`),
  KEY `FKqiius4c7slv9kb2y5mq4wlt7y` (`created_by`),
  KEY `FKsepmjlthp293mlle1x9pacp1g` (`updated_by`),
  CONSTRAINT `FK7xbvhqxg7nv6satydyb0au3t7` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FK9c50llje03tw2uaq3qoonvc7r` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKqiius4c7slv9kb2y5mq4wlt7y` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKs880vo6yb6xbyomq1tjw0jcgd` FOREIGN KEY (`finehead_id`) REFERENCES `finehead` (`id`),
  CONSTRAINT `FKsepmjlthp293mlle1x9pacp1g` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `finehead` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `fine_head_name` varchar(100) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_sge0peklj3q2nb9auweg42p7m` (`fine_head_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `full_payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(38,2) NOT NULL,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `last_updated` datetime(6) DEFAULT NULL,
  `payment_last_date` datetime(6) NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `grade_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_full_payment_discount` (`grade_id`,`academic_year_id`,`school_id`),
  KEY `FK24l54bb5p4wpgyaav374ct9rx` (`academic_year_id`),
  KEY `FKtlg6uut6rhjyi120esvaffq03` (`school_id`),
  KEY `FK96ng77nd4pslgwgxi3syrd4up` (`updated_by`),
  KEY `FK29jl4fuj34pkuhxd84yc77nth` (`created_by`),
  CONSTRAINT `FK24l54bb5p4wpgyaav374ct9rx` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FK29jl4fuj34pkuhxd84yc77nth` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FK4vebi3rxfpmobwk2rqjaiiw9j` FOREIGN KEY (`grade_id`) REFERENCES `grade` (`id`),
  CONSTRAINT `FK96ng77nd4pslgwgxi3syrd4up` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKtlg6uut6rhjyi120esvaffq03` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gconfiguration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_data` text,
  `config_name` varchar(100) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `last_updated` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_gconfig` (`config_name`,`school_id`),
  UNIQUE KEY `UK_r3a7gkhpimenpget1s0bqb0si` (`config_name`),
  KEY `FKnju5quglmi1imgwisoqm17220` (`school_id`),
  CONSTRAINT `FKnju5quglmi1imgwisoqm17220` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grade` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `grade_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_aj2bu738l4tfnobnemkqjshmp` (`grade_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `grievance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `closed_at` datetime(6) DEFAULT NULL,
  `closer_statement_remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `due_date` datetime(6) NOT NULL,
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `academic_student_id` bigint NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `closed_by` bigint DEFAULT NULL,
  `created_by` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnc93cnjg1us3km7ke47roh1kf` (`academic_student_id`),
  KEY `FK9gmx7u37lxfmq2y8gywcsxd14` (`academic_year_id`),
  KEY `FKtr3tlg4l3gnd1wvkv5bi0bpg5` (`closed_by`),
  KEY `FK8otnsm8mqefe2enna9ur0hdsa` (`created_by`),
  KEY `idx_grievance_pending_lookup` (`school_id`,`academic_year_id`,`closed_at`,`due_date`),
  CONSTRAINT `FK80o2s7oky2hjrhfq05h02wf50` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FK8otnsm8mqefe2enna9ur0hdsa` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FK9gmx7u37lxfmq2y8gywcsxd14` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKnc93cnjg1us3km7ke47roh1kf` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`),
  CONSTRAINT `FKtr3tlg4l3gnd1wvkv5bi0bpg5` FOREIGN KEY (`closed_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `holiday` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `holiday_end_date` date DEFAULT NULL,
  `holiday_name` varchar(200) DEFAULT NULL,
  `holiday_start_date` date DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_holiday` (`holiday_name`,`academic_year_id`,`school_id`),
  KEY `FK9wfth1i6ofj2qqywwtums8v14` (`academic_year_id`),
  KEY `FKojr7umoctk2pg8o6c48jmwqks` (`school_id`),
  CONSTRAINT `FK9wfth1i6ofj2qqywwtums8v14` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKojr7umoctk2pg8o6c48jmwqks` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medium` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `medium_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ec6e021ne3lqjgos8dmoyw6dy` (`medium_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mobile_refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `academic_student_id` bigint NOT NULL,
  `token_hash` varchar(64) NOT NULL,
  `issued_at` datetime NOT NULL,
  `expires_at` datetime NOT NULL,
  `revoked` tinyint(1) NOT NULL DEFAULT '0',
  `revoked_at` datetime DEFAULT NULL,
  `last_used_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_mrt_token_hash` (`token_hash`),
  UNIQUE KEY `idx_mrt_token_hash` (`token_hash`),
  KEY `idx_mrt_student` (`academic_student_id`),
  KEY `idx_mrt_cleanup` (`revoked`,`expires_at`),
  CONSTRAINT `fk_mrt_academic_student` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `month_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `priority` int NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `month_master_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrbioc2q5qvyrw2qqv3d6yia3p` (`academic_year_id`),
  KEY `FKq6dt7so0i7ixhwd8nx06ldj14` (`month_master_id`),
  KEY `FKjh3rxb72ydu5e8k78ii4cqujo` (`school_id`),
  KEY `FKg0ubou1b6uerym8w1gwo9y5p7` (`updated_by`),
  KEY `FK3bro20x3fb89nten8ti9tbigp` (`created_by`),
  CONSTRAINT `FK3bro20x3fb89nten8ti9tbigp` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKg0ubou1b6uerym8w1gwo9y5p7` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKjh3rxb72ydu5e8k78ii4cqujo` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKq6dt7so0i7ixhwd8nx06ldj14` FOREIGN KEY (`month_master_id`) REFERENCES `month_master` (`id`),
  CONSTRAINT `FKrbioc2q5qvyrw2qqv3d6yia3p` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `month_master` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `month_code` varchar(10) DEFAULT NULL,
  `month_name` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_qeyrxeg6qadojqclo2advrumj` (`month_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expiry_date` datetime(6) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `used` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_f90ivichjaokvmovxpnlm5nin` (`user_id`),
  CONSTRAINT `FK83nsrttkwkb6ym0anu051mtxn` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `province` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `province_name` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_83ciejd0c6bkbhs58oot8aje4` (`province_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receipt_sequence` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_code` varchar(255) NOT NULL,
  `current_value` int NOT NULL,
  `year` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ofx66keruapi6vyqpv6f2or37` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `school` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` text,
  `board` varchar(100) DEFAULT NULL,
  `contact_person_mobile` varchar(10) DEFAULT NULL,
  `contact_person_name` varchar(100) DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `email` varchar(255) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `logo1` varchar(255) DEFAULT NULL,
  `logo2` varchar(255) DEFAULT NULL,
  `mobile1` varchar(10) DEFAULT NULL,
  `mobile2` varchar(10) DEFAULT NULL,
  `pincode` varchar(6) DEFAULT NULL,
  `school_code` varchar(255) DEFAULT NULL,
  `school_name` varchar(200) DEFAULT NULL,
  `status` varchar(255) NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `city_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `province_id` bigint NOT NULL,
  `show_dashboard_stats` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_lv7h92kf2v8xutn9g7ulbxiq4` (`school_name`),
  KEY `FKayckpg460dxb87jqrmm4ek6j5` (`city_id`),
  KEY `FKl6tbeyt5s8hqlhhil7w4gqekr` (`customer_id`),
  KEY `FK6ayj10ryndp5g2586wjo9q7a5` (`province_id`),
  KEY `FKt420xvkvfkgja33qb4dnfnrq2` (`created_by`),
  KEY `FKg2atxl6gyhvxnx2uc6ms7hdyo` (`updated_by`),
  CONSTRAINT `FK6ayj10ryndp5g2586wjo9q7a5` FOREIGN KEY (`province_id`) REFERENCES `province` (`id`),
  CONSTRAINT `FKayckpg460dxb87jqrmm4ek6j5` FOREIGN KEY (`city_id`) REFERENCES `city` (`id`),
  CONSTRAINT `FKg2atxl6gyhvxnx2uc6ms7hdyo` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKl6tbeyt5s8hqlhhil7w4gqekr` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `FKt420xvkvfkgja33qb4dnfnrq2` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `section` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `section_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_1axhayyo7y5n83ffqpem662r5` (`section_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sibling_discount` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` bigint NOT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `last_updated` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `academic_student_id` bigint NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `discount_class_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `sibling_group_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmyg5y6jm4c7t3ga844i0lo7s9` (`academic_student_id`),
  KEY `FKjwny0rrqkj5qu59h792ebifqp` (`academic_year_id`),
  KEY `FKmcifk1jtgm5sd9ohxsmf29866` (`discount_class_id`),
  KEY `FK89jr58up9p4slkesxjegk3s9u` (`school_id`),
  KEY `FKnsg4d92lk4o1lsq4h3cu9f1lx` (`sibling_group_id`),
  KEY `FKijp3mkwobexsoexax13loy1ka` (`created_by`),
  KEY `FK76uqe2ts38oxhcb7rtggpssc6` (`updated_by`),
  CONSTRAINT `FK76uqe2ts38oxhcb7rtggpssc6` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FK89jr58up9p4slkesxjegk3s9u` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKijp3mkwobexsoexax13loy1ka` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKjwny0rrqkj5qu59h792ebifqp` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKmcifk1jtgm5sd9ohxsmf29866` FOREIGN KEY (`discount_class_id`) REFERENCES `discount_class_map` (`id`),
  CONSTRAINT `FKmyg5y6jm4c7t3ga844i0lo7s9` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`),
  CONSTRAINT `FKnsg4d92lk4o1lsq4h3cu9f1lx` FOREIGN KEY (`sibling_group_id`) REFERENCES `sibling_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sibling_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `group_name` varchar(100) DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_sibling_grp` (`group_name`,`academic_year_id`,`school_id`),
  KEY `FKihinfjw74d7bt038jsnkh936m` (`academic_year_id`),
  KEY `FKqp5rfxa3hy87jg6qkio5vxpio` (`school_id`),
  KEY `FK7lo7lyrxi3aajq7lee8w39klq` (`updated_by`),
  KEY `FKqsg6e2h66gtiq8p9rgkm37blh` (`created_by`),
  CONSTRAINT `FK7lo7lyrxi3aajq7lee8w39klq` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKihinfjw74d7bt038jsnkh936m` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKqp5rfxa3hy87jg6qkio5vxpio` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKqsg6e2h66gtiq8p9rgkm37blh` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sibling_group_student` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `academic_student_id` bigint NOT NULL,
  `sibling_group_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8kvepxwklumwd2y7ty0xvik9p` (`academic_student_id`),
  KEY `FKrhbtveuqitr62r94x50ir0yse` (`sibling_group_id`),
  CONSTRAINT `FK8kvepxwklumwd2y7ty0xvik9p` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`),
  CONSTRAINT `FKrhbtveuqitr62r94x50ir0yse` FOREIGN KEY (`sibling_group_id`) REFERENCES `sibling_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `doc_file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `has_attachment` bit(1) NOT NULL,
  `have_doc_attachment` bit(1) NOT NULL,
  `initiated_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_deleted` bit(1) DEFAULT NULL,
  `seen` bit(1) DEFAULT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `sms_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf2i50j3b4mgk95rt5b7yi99em` (`sms_id`),
  CONSTRAINT `FKf2i50j3b4mgk95rt5b7yi99em` FOREIGN KEY (`sms_id`) REFERENCES `sms_message` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` bigint NOT NULL,
  `message_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `recipient_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `resolution` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sms_heading` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `grade_id` bigint DEFAULT NULL,
  `school_id` bigint NOT NULL,
  `section_id` bigint DEFAULT NULL,
  `due_date` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg0l66pfpinbdtrbnmpoqst59f` (`grade_id`),
  KEY `FKc2mp860wtjka7vnytgk8u32vx` (`school_id`),
  KEY `FKk27krmjni4lhhm023ymbrcvrh` (`section_id`),
  KEY `FK7li32ti4iq59ui9myh9a40mft` (`created_by`),
  KEY `FK3lahcduht6f5uwajx1toiccc1` (`updated_by`),
  CONSTRAINT `FK3lahcduht6f5uwajx1toiccc1` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FK7li32ti4iq59ui9myh9a40mft` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKc2mp860wtjka7vnytgk8u32vx` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKg0l66pfpinbdtrbnmpoqst59f` FOREIGN KEY (`grade_id`) REFERENCES `grade` (`id`),
  CONSTRAINT `FKk27krmjni4lhhm023ymbrcvrh` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_message_attachments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sms_message_id` bigint NOT NULL,
  `stored_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `original_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_type` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_size` bigint NOT NULL,
  `uploaded_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sma_sms_message` (`sms_message_id`),
  CONSTRAINT `fk_sms_message_attachments_sms_message` FOREIGN KEY (`sms_message_id`) REFERENCES `sms_message` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_message_read_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sms_message_id` bigint NOT NULL,
  `academic_student_id` bigint NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `read_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_smrs_message_student` (`sms_message_id`,`academic_student_id`),
  KEY `idx_smrs_student_read` (`academic_student_id`,`is_read`),
  CONSTRAINT `fk_smrs_academic_student` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_smrs_message` FOREIGN KEY (`sms_message_id`) REFERENCES `sms_message` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sms_message_recipients` (
  `sms_message_id` bigint NOT NULL,
  `academic_student_id` bigint NOT NULL,
  KEY `FKbg83xo5v5lwywtb06mb3yc922` (`academic_student_id`),
  KEY `FK9o47nns7001mk0d5cctfb17dx` (`sms_message_id`),
  CONSTRAINT `FK9o47nns7001mk0d5cctfb17dx` FOREIGN KEY (`sms_message_id`) REFERENCES `sms_message` (`id`),
  CONSTRAINT `FKbg83xo5v5lwywtb06mb3yc922` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_discount` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` text,
  `last_updated` datetime(6) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `academic_student_id` bigint NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `discount_head_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `created_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_discount_id` (`academic_student_id`,`discount_head_id`,`academic_year_id`,`school_id`),
  KEY `FKpul3d3gq6caswn5dgf0w5v61o` (`academic_year_id`),
  KEY `FKf5mr3o3jn1tffaayp0ihia5sw` (`discount_head_id`),
  KEY `FK1pccgfkf9toopri86exdaviwt` (`school_id`),
  KEY `FKigy1nlyi80w0a13ph6h6qses` (`created_by`),
  CONSTRAINT `FK1pccgfkf9toopri86exdaviwt` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FKer8k1ydsvm4yo4139ud0dr6i2` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`),
  CONSTRAINT `FKf5mr3o3jn1tffaayp0ihia5sw` FOREIGN KEY (`discount_head_id`) REFERENCES `discounthead` (`id`),
  CONSTRAINT `FKigy1nlyi80w0a13ph6h6qses` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKpul3d3gq6caswn5dgf0w5v61o` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_health_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `academic_student_id` bigint NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `height` int DEFAULT NULL,
  `weight` int DEFAULT NULL,
  `have_health_issues` tinyint(1) NOT NULL DEFAULT '0',
  `have_eye_issue` tinyint(1) NOT NULL DEFAULT '0',
  `health_issue_description` text,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `creation_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_shi_academic_student` (`academic_student_id`),
  KEY `fk_shi_created_by` (`created_by`),
  KEY `fk_shi_updated_by` (`updated_by`),
  KEY `idx_shi_school_year` (`school_id`,`academic_year_id`,`have_health_issues`),
  CONSTRAINT `fk_shi_academic_student` FOREIGN KEY (`academic_student_id`) REFERENCES `academic_students` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_shi_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_shi_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_regional_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_regional` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `creation_date` datetime(6) DEFAULT NULL,
  `father_name_regional` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `mother_name_regional` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `student_name_regional` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `student_id` bigint NOT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_f4c2jd66w556fonvimm20wxcd` (`student_id`),
  KEY `FKp76nege9ouepia16s2w45ljbw` (`created_by`),
  KEY `FKnct7rjxql3qbo5g80bwy4ur7u` (`updated_by`),
  CONSTRAINT `FK8nawh2kc4jdwdcapqombmxcyp` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `FKnct7rjxql3qbo5g80bwy4ur7u` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKp76nege9ouepia16s2w45ljbw` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `aadhar_no` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `account_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `blood_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `body_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `branch_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_by` bigint DEFAULT NULL,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `dob` date DEFAULT NULL,
  `father_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `father_occupation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gender` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `height` int DEFAULT NULL,
  `ifsc_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `landmark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_updated` datetime(6) DEFAULT NULL,
  `mobile1` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile2` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mother_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mother_occupation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nationality` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `passing_year` int DEFAULT NULL,
  `person_contact` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `person_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pincode` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `previous_class` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `previous_school` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `registration_date` datetime(6) DEFAULT NULL,
  `registration_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `relationship` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `religion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remark` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `removal_cause` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `school_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `student_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `tc_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  `uuid` binary(16) NOT NULL,
  `weight` int DEFAULT NULL,
  `academic_year_id` bigint NOT NULL,
  `bank_id` bigint NOT NULL,
  `cast_id` bigint NOT NULL,
  `category_id` bigint NOT NULL,
  `city_id` bigint NOT NULL,
  `grade_id` bigint NOT NULL,
  `medium_id` bigint NOT NULL,
  `province_id` bigint NOT NULL,
  `school_id` bigint NOT NULL,
  `section_id` bigint NOT NULL,
  `user_entity_id` bigint DEFAULT NULL,
  `pen_no` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `apaar_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `distance_from_school` int DEFAULT NULL,
  `father_qualification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mother_qualification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `family_account_id` bigint DEFAULT NULL,
  `psrn` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ib0a6iuka259j8iw9b8ri0pnl` (`uuid`),
  UNIQUE KEY `UK_khupaqt7gruirlcgcmunovkhj` (`user_entity_id`),
  UNIQUE KEY `UK_745w4ud978xucq10hsdilmx1l` (`psrn`),
  UNIQUE KEY `idx_students_psrn_unique` (`psrn`),
  KEY `FKkb6j26xxd2x6ajd9lgvnwla5d` (`academic_year_id`),
  KEY `FKp6cmo50gatx747lgxcvo3k21b` (`bank_id`),
  KEY `FKjd6gn0peayh7m58of1b9ndncd` (`cast_id`),
  KEY `FK4rnfgdsheublniryhijj3nsii` (`category_id`),
  KEY `FK5ndstdmwok4hqbqjtp8vcnjqk` (`city_id`),
  KEY `FK8jq3bo0pfqm2jsjapkygopovx` (`grade_id`),
  KEY `FKm58594iqkwi4o24jwmlt4uyus` (`medium_id`),
  KEY `FK8wm7mkqddtsst9rv68m6dpys9` (`province_id`),
  KEY `FK21tt8xq83kbgwd98k4dybhp1b` (`school_id`),
  KEY `FKsi5jta5q7vg6s009jeiv430dm` (`section_id`),
  KEY `FKqhlekr2jpkjp6dgfj8kv266a5` (`created_by`),
  KEY `FK391a2q0g494jgascm8oh7bidi` (`updated_by`),
  KEY `FKghp4bfxsqmq957f4akykrtu3c` (`family_account_id`),
  KEY `idx_students_school_psrn` (`school_id`,`psrn`),
  CONSTRAINT `FK21tt8xq83kbgwd98k4dybhp1b` FOREIGN KEY (`school_id`) REFERENCES `school` (`id`),
  CONSTRAINT `FK391a2q0g494jgascm8oh7bidi` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FK4rnfgdsheublniryhijj3nsii` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  CONSTRAINT `FK5ndstdmwok4hqbqjtp8vcnjqk` FOREIGN KEY (`city_id`) REFERENCES `city` (`id`),
  CONSTRAINT `FK8jq3bo0pfqm2jsjapkygopovx` FOREIGN KEY (`grade_id`) REFERENCES `grade` (`id`),
  CONSTRAINT `FK8wm7mkqddtsst9rv68m6dpys9` FOREIGN KEY (`province_id`) REFERENCES `province` (`id`),
  CONSTRAINT `FKfvrwtrwfeneo8efhjvdfr7f1` FOREIGN KEY (`user_entity_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKghp4bfxsqmq957f4akykrtu3c` FOREIGN KEY (`family_account_id`) REFERENCES `family_accounts` (`id`),
  CONSTRAINT `FKjd6gn0peayh7m58of1b9ndncd` FOREIGN KEY (`cast_id`) REFERENCES `cast` (`id`),
  CONSTRAINT `FKkb6j26xxd2x6ajd9lgvnwla5d` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_year` (`id`),
  CONSTRAINT `FKm58594iqkwi4o24jwmlt4uyus` FOREIGN KEY (`medium_id`) REFERENCES `medium` (`id`),
  CONSTRAINT `FKp6cmo50gatx747lgxcvo3k21b` FOREIGN KEY (`bank_id`) REFERENCES `bank` (`id`),
  CONSTRAINT `FKqhlekr2jpkjp6dgfj8kv266a5` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `FKsi5jta5q7vg6s009jeiv430dm` FOREIGN KEY (`section_id`) REFERENCES `section` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_config` (
  `config_name` varchar(100) NOT NULL,
  `config_value` varchar(255) NOT NULL,
  `created_date` datetime(6) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `updated_date` datetime(6) DEFAULT NULL,
  `school_ids` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`config_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `screen_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcr1cjyq11u2c2iaamac5nwhpp` (`user_id`,`screen_id`),
  KEY `FKt5q61h5iudyv7sx5bwyhc14m` (`screen_id`),
  CONSTRAINT `FKn8ba4v3gvw1d82t3hofelr82t` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKt5q61h5iudyv7sx5bwyhc14m` FOREIGN KEY (`screen_id`) REFERENCES `app_screen` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_permission_access_types` (
  `permission_id` bigint NOT NULL,
  `access_type` enum('ALL','CREATE','DELETE','EDIT','NOTHING','VIEW') NOT NULL,
  PRIMARY KEY (`permission_id`,`access_type`),
  CONSTRAINT `FKsksgjo9gqccsk4akkq3rvv1yf` FOREIGN KEY (`permission_id`) REFERENCES `user_permission` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  KEY `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`),
  KEY `FKhfh9dx7w3ubf1co1vdev94g3f` (`user_id`),
  CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

