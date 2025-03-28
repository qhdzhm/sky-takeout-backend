/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 8.2.0 : Database - happy_tassie_travel
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`happy_tassie_travel` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `happy_tassie_travel`;

/*Table structure for table `agents` */

DROP TABLE IF EXISTS `agents`;

CREATE TABLE `agents` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `company_name` varchar(100) NOT NULL,
  `contact_person` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `email` varchar(100) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `discount_rate` decimal(3,2) DEFAULT '1.00',
  `status` tinyint DEFAULT '1' COMMENT '1-active, 0-inactive',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  CONSTRAINT `agents_chk_1` CHECK (((`discount_rate` >= 0) and (`discount_rate` <= 1)))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `agents` */

insert  into `agents`(`id`,`username`,`password`,`company_name`,`contact_person`,`phone`,`email`,`address`,`discount_rate`,`status`,`created_at`,`updated_at`) values (1,'agent1','e10adc3949ba59abbe56e057f20f883e','塔斯旅游','张三','13800138001','agent1@example.com','塔斯马尼亚霍巴特市中心123号','0.90',1,'2025-03-21 23:31:51','2025-03-21 23:31:51'),(2,'agent2','e10adc3949ba59abbe56e057f20f883e','悉尼旅游','李四','13800138002','agent2@example.com','悉尼市中心456号','0.85',1,'2025-03-21 23:31:51','2025-03-21 23:31:51'),(3,'agent3','e10adc3949ba59abbe56e057f20f883e','墨尔本旅游','王五','13800138003','agent3@example.com','墨尔本市中心789号','0.88',1,'2025-03-21 23:31:51','2025-03-21 23:31:51');

/*Table structure for table `available_dates` */

DROP TABLE IF EXISTS `available_dates`;

CREATE TABLE `available_dates` (
  `date_id` int NOT NULL AUTO_INCREMENT,
  `group_tour_id` int DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `available_slots` int NOT NULL,
  PRIMARY KEY (`date_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `available_dates` */

insert  into `available_dates`(`date_id`,`group_tour_id`,`start_date`,`end_date`,`price`,`available_slots`) values (1,1,'2023-11-07','2023-11-11','1080.00',16),(2,1,'2023-11-09','2023-11-13','1080.00',16),(3,1,'2023-11-11','2023-11-15','1080.00',16),(4,1,'2023-11-14','2023-11-18','1080.00',16),(5,1,'2023-11-16','2023-11-20','1080.00',16),(6,1,'2023-11-18','2023-11-22','1080.00',16),(7,1,'2023-11-21','2023-11-25','1080.00',16),(8,1,'2023-11-23','2023-11-27','1080.00',16),(9,1,'2023-11-25','2023-11-29','1080.00',16),(10,1,'2023-11-28','2023-12-02','1080.00',16),(11,1,'2023-11-30','2023-12-04','1080.00',16),(12,1,'2023-12-02','2023-12-06','1150.00',16),(13,1,'2023-12-05','2023-12-09','1150.00',16),(14,1,'2023-12-07','2023-12-11','1150.00',16),(15,1,'2023-12-09','2023-12-13','1150.00',16),(16,1,'2023-12-12','2023-12-16','1150.00',16),(17,1,'2023-12-14','2023-12-18','1150.00',16),(18,1,'2023-12-16','2023-12-20','1150.00',16),(19,1,'2023-12-19','2023-12-23','1200.00',16),(20,1,'2023-12-21','2023-12-25','1200.00',16),(21,1,'2023-12-23','2023-12-27','1200.00',16),(22,1,'2023-12-26','2023-12-30','1200.00',16),(23,1,'2023-12-28','2024-01-01','1250.00',16),(24,1,'2023-12-30','2024-01-03','1250.00',16);

/*Table structure for table `bookings` */

DROP TABLE IF EXISTS `bookings`;

CREATE TABLE `bookings` (
  `booking_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `tour_type` enum('day_tour','group_tour') NOT NULL,
  `tour_id` int NOT NULL,
  `booking_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `adults` int NOT NULL DEFAULT '1',
  `children` int DEFAULT '0',
  `total_price` decimal(10,2) NOT NULL,
  `status` enum('pending','confirmed','cancelled','completed') DEFAULT 'pending',
  `payment_status` enum('unpaid','partial','paid') DEFAULT 'unpaid',
  `special_requests` text,
  PRIMARY KEY (`booking_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `bookings` */

insert  into `bookings`(`booking_id`,`user_id`,`tour_type`,`tour_id`,`booking_date`,`start_date`,`end_date`,`adults`,`children`,`total_price`,`status`,`payment_status`,`special_requests`) values (1,1,'group_tour',1,'2023-10-15 14:30:00','2023-11-07','2023-11-11',2,0,'2160.00','confirmed','paid','希望安排靠窗的座位'),(2,2,'group_tour',3,'2023-10-16 10:15:00','2023-11-08','2023-11-11',2,1,'2247.50','confirmed','paid','有一位儿童需要儿童座椅'),(3,3,'day_tour',1,'2023-10-17 09:45:00','2023-11-05',NULL,4,0,'480.00','confirmed','paid','我们有一位素食者'),(4,4,'day_tour',2,'2023-10-18 16:20:00','2023-11-06',NULL,2,2,'300.00','confirmed','paid','需要婴儿座椅'),(5,5,'group_tour',4,'2023-10-19 11:30:00','2023-11-14','2023-11-19',2,0,'2700.00','pending','partial','希望安排蜜月套房');

/*Table structure for table `day_tour_exclusions` */

DROP TABLE IF EXISTS `day_tour_exclusions`;

CREATE TABLE `day_tour_exclusions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day_tour_id` int NOT NULL,
  `description` text NOT NULL,
  `position` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `day_tour_id` (`day_tour_id`),
  CONSTRAINT `day_tour_exclusions_ibfk_1` FOREIGN KEY (`day_tour_id`) REFERENCES `day_tours` (`day_tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_exclusions` */

insert  into `day_tour_exclusions`(`id`,`day_tour_id`,`description`,`position`,`created_at`,`updated_at`) values (1,1,'个人消费',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(2,1,'旅游保险',2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(3,2,'个人消费',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(4,2,'旅游保险',2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(5,3,'个人消费',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(6,3,'旅游保险',2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(7,3,'午餐费用',3,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(8,4,'个人消费',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(9,4,'旅游保险',2,'2025-03-18 13:45:59','2025-03-18 13:45:59');

/*Table structure for table `day_tour_faqs` */

DROP TABLE IF EXISTS `day_tour_faqs`;

CREATE TABLE `day_tour_faqs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day_tour_id` int NOT NULL,
  `question` text NOT NULL,
  `answer` text NOT NULL,
  `position` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `day_tour_id` (`day_tour_id`),
  CONSTRAINT `day_tour_faqs_ibfk_1` FOREIGN KEY (`day_tour_id`) REFERENCES `day_tours` (`day_tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_faqs` */

insert  into `day_tour_faqs`(`id`,`day_tour_id`,`question`,`answer`,`position`,`created_at`,`updated_at`) values (1,1,'这个行程适合老人和儿童吗？','这个行程包含一些中等难度的步行活动，适合身体健康的老人和8岁以上的儿童。',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(2,1,'需要带什么装备？','请穿着舒适的步行鞋，带上防晒霜、帽子、水和相机。',2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(3,2,'这个行程适合游泳吗？','是的，如果天气允许，您可以在酒杯湾海滩游泳，请自备泳衣和毛巾。',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(4,2,'如果下雨怎么办？','我们会根据天气情况调整行程，确保您能够安全舒适地游览景点。',2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(5,3,'萨拉曼卡市场什么时候开放？','市场在周六上午8:30至下午3:00开放，我们的行程会确保在开放时间到达。',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(6,4,'在布鲁尼岛能看到什么动物？','您可能会看到袋鼠、企鹅、海豚和各种鸟类，但野生动物观赏不能保证。',1,'2025-03-18 13:45:59','2025-03-18 13:45:59');

/*Table structure for table `day_tour_highlights` */

DROP TABLE IF EXISTS `day_tour_highlights`;

CREATE TABLE `day_tour_highlights` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day_tour_id` int NOT NULL,
  `description` text NOT NULL,
  `position` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `day_tour_id` (`day_tour_id`),
  CONSTRAINT `day_tour_highlights_ibfk_1` FOREIGN KEY (`day_tour_id`) REFERENCES `day_tours` (`day_tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_highlights` */

insert  into `day_tour_highlights`(`id`,`day_tour_id`,`description`,`position`,`created_at`,`updated_at`) values (1,1,'探索世界遗产摇篮山国家公园的壮观景色',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(2,2,'参观澳大利亚最美丽的海滩之一',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(3,3,'参观历史悠久的萨拉曼卡市场',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(4,4,'享受专业导游的详细讲解和当地文化体验',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(8,10,'非常好玩',NULL,'2025-03-21 19:42:36','2025-03-21 19:42:36'),(9,1,'探索世界遗产摇篮山国家公园的壮观景色,meibushengshou',NULL,'2025-03-22 17:28:58','2025-03-22 17:28:58');

/*Table structure for table `day_tour_images` */

DROP TABLE IF EXISTS `day_tour_images`;

CREATE TABLE `day_tour_images` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day_tour_id` int NOT NULL,
  `image_url` varchar(255) NOT NULL,
  `thumbnail_url` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_primary` tinyint(1) DEFAULT '0',
  `position` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `day_tour_id` (`day_tour_id`),
  CONSTRAINT `day_tour_images_ibfk_1` FOREIGN KEY (`day_tour_id`) REFERENCES `day_tours` (`day_tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_images` */

insert  into `day_tour_images`(`id`,`day_tour_id`,`image_url`,`thumbnail_url`,`description`,`is_primary`,`position`,`created_at`,`updated_at`) values (1,1,'https://example.com/cradle-mountain1.jpg','https://example.com/cradle-mountain1-thumb.jpg','摇篮山全景',1,1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(2,1,'https://example.com/cradle-mountain2.jpg','https://example.com/cradle-mountain2-thumb.jpg','多芬湖景色',0,2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(3,1,'https://example.com/cradle-mountain3.jpg','https://example.com/cradle-mountain3-thumb.jpg','步道风光',0,3,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(4,2,'https://example.com/wineglass-bay1.jpg','https://example.com/wineglass-bay1-thumb.jpg','酒杯湾全景',1,1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(5,2,'https://example.com/wineglass-bay2.jpg','https://example.com/wineglass-bay2-thumb.jpg','酒杯湾海滩',0,2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(6,3,'https://example.com/hobart1.jpg','https://example.com/hobart1-thumb.jpg','霍巴特港口',1,1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(7,3,'https://example.com/hobart2.jpg','https://example.com/hobart2-thumb.jpg','萨拉曼卡市场',0,2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(8,4,'https://example.com/bruny1.jpg','https://example.com/bruny1-thumb.jpg','布鲁尼岛灯塔',1,1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(9,4,'https://example.com/bruny2.jpg','https://example.com/bruny2-thumb.jpg','企鹅栖息地',0,2,'2025-03-18 13:45:59','2025-03-18 13:45:59');

/*Table structure for table `day_tour_inclusions` */

DROP TABLE IF EXISTS `day_tour_inclusions`;

CREATE TABLE `day_tour_inclusions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day_tour_id` int NOT NULL,
  `description` text NOT NULL,
  `position` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `day_tour_id` (`day_tour_id`),
  CONSTRAINT `day_tour_inclusions_ibfk_1` FOREIGN KEY (`day_tour_id`) REFERENCES `day_tours` (`day_tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_inclusions` */

insert  into `day_tour_inclusions`(`id`,`day_tour_id`,`description`,`position`,`created_at`,`updated_at`) values (1,1,'专业英语导游',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(3,1,'国家公园门票',3,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(4,1,'午餐',4,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(5,2,'专业英语导游',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(6,2,'往返酒店接送',2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(7,2,'国家公园门票',3,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(8,2,'午餐和小吃',4,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(9,3,'专业英语导游',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(10,3,'往返酒店接送',2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(11,3,'景点门票',3,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(12,4,'专业英语导游',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(13,4,'往返渡轮票',2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(14,4,'野生动物园门票',3,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(15,4,'午餐',4,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(16,10,'旅店',NULL,'2025-03-21 19:43:24','2025-03-21 19:43:24');

/*Table structure for table `day_tour_itinerary` */

DROP TABLE IF EXISTS `day_tour_itinerary`;

CREATE TABLE `day_tour_itinerary` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day_tour_id` int NOT NULL,
  `time_slot` varchar(50) NOT NULL,
  `activity` text NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `description` text,
  `position` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `day_tour_id` (`day_tour_id`),
  CONSTRAINT `day_tour_itinerary_ibfk_1` FOREIGN KEY (`day_tour_id`) REFERENCES `day_tours` (`day_tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_itinerary` */

insert  into `day_tour_itinerary`(`id`,`day_tour_id`,`time_slot`,`activity`,`location`,`description`,`position`,`created_at`,`updated_at`) values (1,1,'07:30-08:00','酒店接客','霍巴特市区','舒适大巴接送，开始愉快的一天',1,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(2,1,'10:00-10:30','到达摇篮山国家公园','摇篮山游客中心','了解国家公园的历史和生态',2,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(3,1,'10:30-12:30','徒步探索','鸽子湖步道','沿着风景优美的步道漫步，欣赏原始丛林和多芬湖',3,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(4,1,'12:30-13:30','午餐时间','国家公园餐厅','享用当地特色午餐',4,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(5,1,'13:30-15:30','继续探索','摇篮山步道','攀登至观景台，欣赏壮观的山景',5,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(6,1,'15:30-17:30','返回霍巴特','途经薰衣草农场','返回的路上可欣赏沿途风光，并短暂停留在薰衣草农场',6,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(7,2,'07:30-08:00','酒店接客','霍巴特市区','舒适大巴接送，开始愉快的一天',1,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(8,2,'09:30-10:00','到达菲欣纳国家公园','菲欣纳国家公园入口','介绍国家公园和酒杯湾的历史与地理',2,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(9,2,'10:00-11:30','徒步前往酒杯湾观景台','酒杯湾步道','中等强度的上坡徒步，沿途欣赏岩石地形和森林景观',3,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(10,2,'11:30-12:00','酒杯湾观景台','观景台','在著名的观景台欣赏酒杯湾的标志性全景，拍照留念',4,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(11,2,'12:00-13:00','午餐时间','观景台野餐区','享用准备好的午餐，休息补充能量',5,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(12,2,'13:00-14:30','下山前往海滩','下山步道','选择性活动：徒步下山至酒杯湾海滩，体验澳大利亚最美的沙滩之一',6,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(13,2,'14:30-15:30','海滩活动时间','酒杯湾海滩','在洁白的沙滩上放松，游泳或只是欣赏美景',7,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(14,2,'15:30-17:30','返回霍巴特','沿海公路','返回的路上可欣赏塔斯马尼亚东海岸的壮丽景色',8,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(15,3,'09:00-09:30','酒店接客','霍巴特市区','市区集合，开始霍巴特市区一日游',1,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(16,3,'09:30-10:30','参观萨拉曼卡市场','萨拉曼卡广场','探索澳大利亚最好的户外市场之一，品尝当地美食，购买手工艺品',2,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(17,3,'10:30-11:30','参观皇家植物园','皇家植物园','漫步于美丽的植物园，欣赏塔斯马尼亚原生植物',3,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(18,3,'11:30-12:30','午餐时间','萨拉曼卡广场餐厅','在历史悠久的萨拉曼卡广场附近享用午餐（自费）',4,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(19,3,'12:30-14:00','参观塔斯马尼亚博物馆和艺术馆','TMAG','了解塔斯马尼亚丰富的历史和文化',5,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(20,3,'14:00-15:00','参观霍巴特古堡','霍巴特古堡','登上电池角俯瞰霍巴特港口的壮丽景色',6,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(21,3,'15:00-16:00','漫步霍巴特港口','构成港口','欣赏充满活力的水滨区，参观历史建筑',7,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(22,3,'16:00-17:00','返回酒店','霍巴特市区','结束充实的一天',8,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(23,4,'07:30-08:00','酒店接客','霍巴特市区','舒适大巴接送，开始愉快的一天',1,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(24,4,'08:30-09:15','乘坐渡轮前往布鲁尼岛','凯特林渡轮码头','乘坐渡轮横渡D\'Entrecasteaux海峡，欣赏海景',2,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(25,4,'09:15-10:00','前往南布鲁尼国家公园','布鲁尼岛公路','途经布鲁尼岛的风景名胜',3,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(26,4,'10:00-11:00','参观布鲁尼岛灯塔','南布鲁尼国家公园','参观澳大利亚最南端的灯塔，站在悬崖上欣赏壮观的海景',4,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(27,4,'11:00-12:00','观鸟活动','海岸步道','沿着海岸线徒步，观察各种海鸟和可能的海洋生物',5,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(28,4,'12:00-13:00','午餐时间','布鲁尼岛咖啡馆','享用新鲜的海鲜午餐',6,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(29,4,'13:00-14:30','参观布鲁尼岛巧克力工厂','巧克力工厂','参观工厂，了解巧克力制作过程并品尝美味的巧克力',7,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(30,4,'14:30-15:30','参观布鲁尼岛果酱工厂','果酱工厂','品尝和购买当地特色的浆果果酱',8,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(31,4,'15:30-16:15','返回码头','凯特林渡轮码头','乘坐渡轮返回',9,'2025-03-18 13:47:15','2025-03-18 13:47:15'),(32,4,'16:15-17:00','返回霍巴特','霍巴特市区','返回酒店，结束愉快的布鲁尼岛之旅',10,'2025-03-18 13:47:15','2025-03-18 13:47:15');

/*Table structure for table `day_tour_schedule` */

DROP TABLE IF EXISTS `day_tour_schedule`;

CREATE TABLE `day_tour_schedule` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day_tour_id` int DEFAULT NULL,
  `schedule_date` date NOT NULL,
  `available_seats` int NOT NULL,
  `status` int DEFAULT '1' COMMENT '0:未开放, 1:开放预订, 2:已满座, 3:已结束',
  `remarks` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_schedule` */

insert  into `day_tour_schedule`(`id`,`day_tour_id`,`schedule_date`,`available_seats`,`status`,`remarks`) values (1,1,'2023-11-05',20,1,'开放预订'),(2,1,'2023-11-06',20,1,'开放预订'),(3,1,'2023-11-07',20,1,'开放预订'),(4,1,'2023-11-08',20,1,'开放预订'),(5,1,'2023-11-09',20,1,'开放预订'),(6,2,'2023-11-05',15,1,'开放预订'),(7,2,'2023-11-06',15,1,'开放预订'),(8,2,'2023-11-07',15,1,'开放预订'),(9,3,'2023-11-05',10,1,'开放预订'),(10,3,'2023-11-06',10,1,'开放预订');

/*Table structure for table `day_tour_schedules` */

DROP TABLE IF EXISTS `day_tour_schedules`;

CREATE TABLE `day_tour_schedules` (
  `schedule_id` int NOT NULL AUTO_INCREMENT,
  `day_tour_id` int DEFAULT NULL,
  `date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time DEFAULT NULL,
  `available_slots` int NOT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`schedule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_schedules` */

insert  into `day_tour_schedules`(`schedule_id`,`day_tour_id`,`date`,`start_time`,`end_time`,`available_slots`,`price`) values (1,1,'2023-11-05','08:00:00','16:00:00',20,'120.00'),(2,1,'2023-11-06','08:00:00','16:00:00',20,'120.00'),(3,1,'2023-11-07','08:00:00','16:00:00',20,'120.00'),(4,1,'2023-11-08','08:00:00','16:00:00',20,'120.00'),(5,1,'2023-11-09','08:00:00','16:00:00',20,'120.00'),(6,1,'2023-11-10','08:00:00','16:00:00',20,'120.00'),(7,1,'2023-11-11','08:00:00','16:00:00',20,'120.00'),(8,2,'2023-11-05','09:00:00','15:00:00',20,'100.00'),(9,2,'2023-11-06','09:00:00','15:00:00',20,'100.00'),(10,2,'2023-11-07','09:00:00','15:00:00',20,'100.00'),(11,2,'2023-11-08','09:00:00','15:00:00',20,'100.00'),(12,2,'2023-11-09','09:00:00','15:00:00',20,'100.00'),(13,2,'2023-11-10','09:00:00','15:00:00',20,'100.00'),(14,2,'2023-11-11','09:00:00','15:00:00',20,'100.00'),(15,3,'2023-11-05','10:00:00','15:00:00',20,'80.00'),(16,3,'2023-11-06','10:00:00','15:00:00',20,'80.00'),(17,3,'2023-11-07','10:00:00','15:00:00',20,'80.00'),(18,3,'2023-11-08','10:00:00','15:00:00',20,'80.00'),(19,3,'2023-11-09','10:00:00','15:00:00',20,'80.00'),(20,3,'2023-11-10','10:00:00','15:00:00',20,'80.00'),(21,3,'2023-11-11','10:00:00','15:00:00',20,'80.00');

/*Table structure for table `day_tour_suitable_relation` */

DROP TABLE IF EXISTS `day_tour_suitable_relation`;

CREATE TABLE `day_tour_suitable_relation` (
  `relation_id` int NOT NULL AUTO_INCREMENT,
  `day_tour_id` int DEFAULT NULL,
  `suitable_id` int DEFAULT NULL,
  PRIMARY KEY (`relation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_suitable_relation` */

insert  into `day_tour_suitable_relation`(`relation_id`,`day_tour_id`,`suitable_id`) values (5,2,1),(6,2,2),(7,2,3),(8,3,1),(9,3,5),(10,3,3),(11,4,1),(12,4,6),(13,4,2),(14,5,2),(15,5,3),(16,5,4),(17,6,1),(18,6,5),(19,6,3),(20,7,1),(21,7,2),(22,7,3),(23,7,5),(24,8,1),(25,8,2),(26,8,3),(27,8,5),(50,1,1),(51,10,4);

/*Table structure for table `day_tour_theme_relation` */

DROP TABLE IF EXISTS `day_tour_theme_relation`;

CREATE TABLE `day_tour_theme_relation` (
  `relation_id` int NOT NULL AUTO_INCREMENT,
  `day_tour_id` int DEFAULT NULL,
  `theme_id` int DEFAULT NULL,
  PRIMARY KEY (`relation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_theme_relation` */

insert  into `day_tour_theme_relation`(`relation_id`,`day_tour_id`,`theme_id`) values (4,2,1),(5,2,6),(6,2,5),(7,3,2),(8,3,3),(9,3,4),(10,4,1),(11,4,6),(12,4,5),(13,5,1),(14,5,5),(15,5,6),(16,6,3),(17,6,2),(18,7,1),(19,7,5),(20,7,6),(21,8,4),(22,8,2),(28,2,1),(29,2,6),(30,2,5),(31,3,2),(32,3,3),(33,3,4),(34,4,1),(35,4,6),(36,4,5),(37,5,1),(38,5,5),(39,5,6),(40,6,3),(41,6,2),(42,7,1),(43,7,5),(44,7,6),(45,8,4),(46,8,2),(77,1,1);

/*Table structure for table `day_tour_themes` */

DROP TABLE IF EXISTS `day_tour_themes`;

CREATE TABLE `day_tour_themes` (
  `theme_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`theme_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_themes` */

insert  into `day_tour_themes`(`theme_id`,`name`) values (1,'自然风光'),(2,'城市观光'),(3,'历史文化'),(4,'美食体验'),(5,'摄影之旅'),(6,'户外活动');

/*Table structure for table `day_tour_tips` */

DROP TABLE IF EXISTS `day_tour_tips`;

CREATE TABLE `day_tour_tips` (
  `id` int NOT NULL AUTO_INCREMENT,
  `day_tour_id` int NOT NULL,
  `description` text NOT NULL,
  `position` int DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `day_tour_id` (`day_tour_id`),
  CONSTRAINT `day_tour_tips_ibfk_1` FOREIGN KEY (`day_tour_id`) REFERENCES `day_tours` (`day_tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tour_tips` */

insert  into `day_tour_tips`(`id`,`day_tour_id`,`description`,`position`,`created_at`,`updated_at`) values (1,1,'带上足够的水和小吃，因为国家公园内购物选择有限。',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(2,1,'穿着舒适的徒步鞋，并准备多层衣物，因为山区天气可能变化较大。',2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(3,2,'如果计划游泳，请带上泳衣、毛巾和防水防晒霜。',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(4,2,'带上相机，酒杯湾是塔斯马尼亚最上镜的地点之一。',2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(5,3,'在萨拉曼卡市场可以找到许多当地手工艺品，带些现金很有用。',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(6,3,'行程包括一些步行，请穿着舒适的鞋子。',2,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(7,4,'布鲁尼岛气候多变，建议携带防风防水外套。',1,'2025-03-18 13:45:59','2025-03-18 13:45:59'),(8,4,'早上出发的行程通常能看到更多野生动物。',2,'2025-03-18 13:45:59','2025-03-18 13:45:59');

/*Table structure for table `day_tours` */

DROP TABLE IF EXISTS `day_tours`;

CREATE TABLE `day_tours` (
  `day_tour_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `location` varchar(100) NOT NULL,
  `description` text,
  `price` decimal(10,2) NOT NULL,
  `duration` varchar(50) NOT NULL,
  `rating` decimal(2,1) DEFAULT '0.0',
  `image_url` varchar(255) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `region_id` int DEFAULT NULL,
  `departure_address` varchar(255) DEFAULT NULL,
  `guide_fee` decimal(10,2) DEFAULT NULL,
  `guide_id` int DEFAULT NULL,
  PRIMARY KEY (`day_tour_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `day_tours` */

insert  into `day_tours`(`day_tour_id`,`name`,`location`,`description`,`price`,`duration`,`rating`,`image_url`,`category`,`is_active`,`created_at`,`updated_at`,`region_id`,`departure_address`,`guide_fee`,`guide_id`) values (1,'摇篮山国家公园一日游','塔斯马尼亚北部','世界遗产，拥有壮观的山脉和原始森林，是徒步爱好者的天堂。','120.00','8小时','4.9','/images/tour/bali-1.png','自然风光',1,'2025-03-15 20:47:33','2025-03-22 17:30:02',1,'霍巴特市中心旅游集合点','50.00',NULL),(2,'酒杯湾海滩一日游','塔斯马尼亚东部','拥有世界上最美丽的海滩之一，湛蓝的海水和洁白的沙滩形成鲜明对比。','100.00','6小时','2.8','/images/tour/Tokyo.png','海滩',1,'2025-03-15 20:47:33','2025-03-18 15:24:16',3,'霍巴特市中心旅游集合点','45.00',NULL),(3,'霍巴特市区观光一日游','霍巴特','塔斯马尼亚首府，拥有丰富的历史建筑和萨拉曼卡市场。','80.00','5小时','4.6','/images/tour/bangkok.png','城市观光',1,'2025-03-15 20:47:33','2025-03-15 20:47:33',7,'霍巴特市中心旅游集合点','40.00',NULL),(4,'布鲁尼岛一日游','塔斯马尼亚东南部','岛上有丰富的野生动物和美丽的海岸线，是观赏企鹅的好地方。','150.00','9小时','4.7','/images/tour/cancun.png','岛屿',1,'2025-03-15 20:47:33','2025-03-15 20:47:33',6,'霍巴特市中心旅游集合点','55.00',NULL),(5,'菲欣纳国家公园一日游','塔斯马尼亚东部','拥有壮观的粉红色花岗岩山脉和清澈的海湾，是徒步和摄影的绝佳地点。','110.00','7小时','3.8','/images/tour/nah-trang.png','自然风光',1,'2025-03-15 20:47:33','2025-03-18 15:24:16',3,'霍巴特市中心旅游集合点','50.00',NULL),(6,'塔斯曼半岛历史一日游','塔斯马尼亚东南部','以其戏剧性的海岸线和历史遗迹而闻名，包括亚瑟港历史遗址。','130.00','8小时','4.7','/images/tour/phuket.png','历史文化',1,'2025-03-15 20:47:33','2025-03-15 20:47:33',6,'霍巴特市中心旅游集合点','50.00',NULL),(7,'威灵顿山徒步一日游','霍巴特','霍巴特的标志性山脉，可俯瞰整个城市和德文特河。','90.00','6小时','2.5','/images/tour/paris.png','自然风光',1,'2025-03-15 20:47:33','2025-03-18 15:24:16',7,'霍巴特市中心旅游集合点','45.00',NULL),(8,'萨拉曼卡市场美食一日游','霍巴特','澳大利亚最好的户外市场之一，提供当地美食、手工艺品和新鲜农产品。','60.00','4小时','3.6','/images/tour/malaysia.png','购物美食',1,'2025-03-15 20:47:33','2025-03-18 15:24:16',7,'霍巴特市中心旅游集合点','35.00',NULL),(10,'罗素瀑布徒步一日you','塔斯马尼亚中部','位于山谷中的壮观瀑布，周围环绕着茂密的雨林。','80.00','10小时','1.5','/images/tour/Tokyo.png','自然风光',1,'2025-03-15 20:47:33','2025-03-23 11:24:22',5,'霍巴特市中心旅游集合点','40.00',NULL);

/*Table structure for table `discount_price_history` */

DROP TABLE IF EXISTS `discount_price_history`;

CREATE TABLE `discount_price_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `agent_id` int NOT NULL,
  `tour_id` int NOT NULL,
  `tour_type` enum('day_tour','group_tour') NOT NULL,
  `original_price` decimal(10,2) NOT NULL,
  `discounted_price` decimal(10,2) NOT NULL,
  `discount_rate` decimal(5,2) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_agent_tour` (`agent_id`,`tour_id`,`tour_type`),
  KEY `fk_agent_price` (`agent_id`),
  CONSTRAINT `fk_agent_price` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_discount_agent` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `discount_price_history` */

/*Table structure for table `employees` */

DROP TABLE IF EXISTS `employees`;

CREATE TABLE `employees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `sex` varchar(10) DEFAULT NULL,
  `id_number` varchar(20) DEFAULT NULL,
  `status` int DEFAULT '1',
  `role` int DEFAULT NULL,
  `work_status` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `create_user` bigint DEFAULT NULL,
  `update_user` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `employees` */

insert  into `employees`(`id`,`username`,`name`,`password`,`phone`,`sex`,`id_number`,`status`,`role`,`work_status`,`create_time`,`update_time`,`create_user`,`update_user`) values (1,'zhangsan','张三','e10adc3949ba59abbe56e057f20f883e','13800138001','男','110101199001011234',1,0,0,'2025-03-11 17:46:10','2025-03-11 17:46:10',1,1),(2,'lisi','李四','e10adc3949ba59abbe56e057f20f883e','13800138002','女','110101199002022345',1,1,0,'2025-03-11 17:46:10','2025-03-11 17:46:10',1,1),(3,'wangwu','王五','e10adc3949ba59abbe56e057f20f883e','13800138003','男','110101199003033456',1,2,0,'2025-03-11 17:46:10','2025-03-11 17:46:10',1,1),(4,'zhaoliu','赵六','e10adc3949ba59abbe56e057f20f883e','13800138004','女','110101199004044567',1,1,0,'2025-03-11 17:46:10',NULL,1,NULL),(5,'sunqi','孙七','e10adc3949ba59abbe56e057f20f883e','13800138005','男','110101199005055678',1,1,1,'2025-03-11 17:46:10',NULL,1,NULL),(6,'zhouba','周八','e10adc3949ba59abbe56e057f20f883e','0478759693','男','110101199006066789',1,1,1,'2025-03-11 17:46:10',NULL,1,NULL),(7,'wujiu','吴九','e10adc3949ba59abbe56e057f20f883e','0478759693','女','110101199007077890',1,NULL,1,'2025-03-11 17:46:10',NULL,1,NULL),(8,'zzz','在','e10adc3949ba59abbe56e057f20f883e','0111111111','1','110101199008088901',1,NULL,NULL,'2025-03-11 17:46:10',NULL,1,NULL),(9,'qhdzhm','tom','e10adc3949ba59abbe56e057f20f883e','0478759693','1','123123',1,0,0,NULL,NULL,NULL,NULL);

/*Table structure for table `group_tour_day_tour_relation` */

DROP TABLE IF EXISTS `group_tour_day_tour_relation`;

CREATE TABLE `group_tour_day_tour_relation` (
  `id` int NOT NULL AUTO_INCREMENT,
  `group_tour_id` int NOT NULL COMMENT '团队游ID',
  `day_tour_id` int NOT NULL COMMENT '一日游ID',
  `day_number` int NOT NULL COMMENT '天数（第几天）',
  `is_optional` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为可选项（0-必选，1-可选）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_group_tour_id` (`group_tour_id`),
  KEY `idx_day_tour_id` (`day_tour_id`),
  CONSTRAINT `fk_gdt_day_tour_id` FOREIGN KEY (`day_tour_id`) REFERENCES `day_tours` (`day_tour_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_gdt_group_tour_id` FOREIGN KEY (`group_tour_id`) REFERENCES `group_tours` (`group_tour_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='团队游与一日游关联表';

/*Data for the table `group_tour_day_tour_relation` */

insert  into `group_tour_day_tour_relation`(`id`,`group_tour_id`,`day_tour_id`,`day_number`,`is_optional`,`create_time`,`update_time`) values (1,1,3,1,0,'2025-03-23 10:41:13','2025-03-23 10:41:13'),(2,1,5,2,0,'2025-03-23 10:41:13','2025-03-23 10:41:13'),(3,1,2,3,0,'2025-03-23 10:41:13','2025-03-23 10:41:13'),(4,1,1,3,1,'2025-03-23 10:41:13','2025-03-23 10:41:13'),(5,2,4,1,0,'2025-03-23 10:41:13','2025-03-23 10:41:13'),(6,2,1,2,0,'2025-03-23 10:41:13','2025-03-23 10:41:13'),(7,1,1,1,0,'2025-03-23 10:48:44','2025-03-23 10:48:44'),(8,1,2,2,0,'2025-03-23 10:48:44','2025-03-23 10:48:44'),(9,1,3,3,0,'2025-03-23 10:48:44','2025-03-23 10:48:44'),(10,1,4,3,1,'2025-03-23 10:48:44','2025-03-23 10:48:44'),(11,2,2,1,0,'2025-03-23 10:48:44','2025-03-23 10:48:44'),(12,2,3,2,0,'2025-03-23 10:48:44','2025-03-23 10:48:44'),(13,2,5,3,0,'2025-03-23 10:48:44','2025-03-23 10:48:44'),(14,1,1,1,0,'2025-03-23 10:48:48','2025-03-23 10:48:48'),(15,1,2,2,0,'2025-03-23 10:48:48','2025-03-23 10:48:48'),(16,1,3,3,0,'2025-03-23 10:48:48','2025-03-23 10:48:48'),(17,1,4,3,1,'2025-03-23 10:48:48','2025-03-23 10:48:48'),(18,2,2,1,0,'2025-03-23 10:48:48','2025-03-23 10:48:48'),(19,2,3,2,0,'2025-03-23 10:48:48','2025-03-23 10:48:48'),(20,2,5,3,0,'2025-03-23 10:48:48','2025-03-23 10:48:48');

/*Table structure for table `group_tour_suitable_relation` */

DROP TABLE IF EXISTS `group_tour_suitable_relation`;

CREATE TABLE `group_tour_suitable_relation` (
  `relation_id` int NOT NULL AUTO_INCREMENT,
  `group_tour_id` int DEFAULT NULL,
  `suitable_id` int DEFAULT NULL,
  PRIMARY KEY (`relation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `group_tour_suitable_relation` */

insert  into `group_tour_suitable_relation`(`relation_id`,`group_tour_id`,`suitable_id`) values (1,1,1),(2,1,2),(3,1,3),(4,2,3),(5,2,2),(6,3,1),(7,3,2),(8,3,5),(9,4,2),(10,4,3),(11,5,1),(12,5,6),(13,6,5),(14,6,3),(15,7,3),(16,7,4),(17,8,1),(18,8,6),(19,9,2);

/*Table structure for table `group_tour_theme_relation` */

DROP TABLE IF EXISTS `group_tour_theme_relation`;

CREATE TABLE `group_tour_theme_relation` (
  `relation_id` int NOT NULL AUTO_INCREMENT,
  `group_tour_id` int DEFAULT NULL,
  `theme_id` int DEFAULT NULL,
  PRIMARY KEY (`relation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `group_tour_theme_relation` */

insert  into `group_tour_theme_relation`(`relation_id`,`group_tour_id`,`theme_id`) values (1,1,1),(2,1,3),(3,2,2),(4,2,1),(5,3,1),(6,3,3),(7,4,4),(8,4,3),(9,5,2),(10,5,5),(11,6,3),(12,6,1),(13,7,2),(14,7,1),(15,8,5),(16,8,1),(17,9,6),(18,9,1);

/*Table structure for table `group_tour_themes` */

DROP TABLE IF EXISTS `group_tour_themes`;

CREATE TABLE `group_tour_themes` (
  `theme_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`theme_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `group_tour_themes` */

insert  into `group_tour_themes`(`theme_id`,`name`) values (1,'休闲度假'),(2,'探险体验'),(3,'文化探索'),(4,'美食之旅'),(5,'亲子游'),(6,'蜜月旅行');

/*Table structure for table `group_tours` */

DROP TABLE IF EXISTS `group_tours`;

CREATE TABLE `group_tours` (
  `group_tour_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `short_title` varchar(50) DEFAULT NULL,
  `description` text,
  `price` decimal(10,2) NOT NULL,
  `discounted_price` decimal(10,2) DEFAULT NULL,
  `discount_percentage` int DEFAULT NULL,
  `duration` varchar(50) NOT NULL,
  `days` int NOT NULL,
  `nights` int NOT NULL,
  `rating` decimal(2,1) DEFAULT '0.0',
  `reviews_count` int DEFAULT '0',
  `tour_code` varchar(20) DEFAULT NULL,
  `departure_info` varchar(100) DEFAULT NULL,
  `group_size` varchar(50) DEFAULT NULL,
  `language` varchar(50) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `location` varchar(100) DEFAULT NULL,
  `category` varchar(100) DEFAULT NULL,
  `departure_address` varchar(255) DEFAULT NULL,
  `guide_fee` decimal(10,2) DEFAULT NULL,
  `guide_id` int DEFAULT NULL,
  PRIMARY KEY (`group_tour_id`),
  UNIQUE KEY `tour_code` (`tour_code`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `group_tours` */

insert  into `group_tours`(`group_tour_id`,`title`,`short_title`,`description`,`price`,`discounted_price`,`discount_percentage`,`duration`,`days`,`nights`,`rating`,`reviews_count`,`tour_code`,`departure_info`,`group_size`,`language`,`image_url`,`is_active`,`created_at`,`updated_at`,`location`,`category`,`departure_address`,`guide_fee`,`guide_id`) values (1,'塔斯马尼亚精华5日游','塔岛精华游','塔斯马尼亚是澳大利亚最小的州，却拥有令人惊叹的自然风光和丰富的历史文化。这个5日精华之旅将带您探索塔斯马尼亚的标志性景点，从霍巴特的历史街区到菲欣纳半岛的壮丽海岸线，从摇篮山的原始森林到布鲁尼岛的野生动物。您将体验当地特色美食，了解独特的历史文化，并在舒适的住宿中放松身心。这是一次完美的塔斯马尼亚体验，适合所有热爱自然和文化的旅行者。','1200.00','1080.00',10,'5天4晚',5,4,'4.8',56,'TAS-5D-01','每周二、四、六出发','2-16人','中文导游','/images/popular/Discover Singapore.png',1,'2025-03-15 20:48:16','2025-03-15 20:48:16','霍巴特出发，环岛游览','精品团队,自然风光','霍巴特国际机场','200.00',NULL),(2,'塔斯马尼亚西海岸探险3日游','西海岸探险','探索塔斯马尼亚西海岸的自然奇观和历史遗迹','880.00','790.00',10,'3天2晚',3,2,'4.6',42,'TAS-3D-01','每周一、五出发','2-12人','中文导游','/images/popular/Kiwiana Panorama.jpg',1,'2025-03-15 20:48:16','2025-03-15 20:48:16','斯特拉恩出发，西海岸探索','精品团队,探险','斯特拉恩旅游中心','150.00',NULL),(3,'塔斯马尼亚东海岸休闲4日游','东海岸休闲','体验塔斯马尼亚东海岸的美丽海滩和休闲氛围','950.00','899.00',5,'4天3晚',4,3,'4.5',38,'TAS-4D-01','每周三、日出发','2-14人','中文导游','/images/popular/Anchorage To Quito.jpg',1,'2025-03-15 20:48:16','2025-03-15 20:48:16','霍巴特出发，东海岸线路','精品团队,海滩度假','霍巴特国际机场','180.00',NULL),(4,'塔斯马尼亚美食与葡萄酒之旅','美食葡萄酒','品尝塔斯马尼亚顶级美食和葡萄酒的专业之旅','1500.00','1350.00',10,'6天5晚',6,5,'4.9',64,'TAS-6D-01','每周二、六出发','2-10人','中文导游','/images/popular/Anchorage To La Paz.jpg',1,'2025-03-15 20:48:16','2025-03-15 20:48:16','霍巴特出发，环岛美食探索','精品团队,美食体验','霍巴特国际机场','220.00',NULL),(5,'塔斯马尼亚野生动物观赏团','野生动物','近距离观察塔斯马尼亚特有野生动物的专业之旅','980.00','930.00',5,'4天3晚',4,3,'4.7',51,'TAS-4D-02','每周一、四、日出发','2-12人','中文导游','/images/popular/Cuzco To Anchorage.jpg',1,'2025-03-15 20:48:16','2025-03-15 20:48:16','霍巴特出发，多地野生动物保护区','精品团队,野生动物','霍巴特国际机场','180.00',NULL),(6,'塔斯马尼亚历史文化探索之旅','历史文化','深入了解塔斯马尼亚丰富历史和文化遗产','1100.00','990.00',10,'5天4晚',5,4,'4.4',35,'TAS-5D-02','每周三、日出发','2-14人','中文导游','/images/popular/Anchorage To Ushuaia.jpg',1,'2025-03-15 20:48:16','2025-03-15 20:48:16','霍巴特出发，历史遗迹探索','精品团队,文化体验','霍巴特国际机场','200.00',NULL),(7,'塔斯马尼亚徒步探险7日游','徒步探险','挑战塔斯马尼亚最美徒步路线的专业探险之旅','1680.00','1580.00',6,'7天6晚',7,6,'4.8',47,'TAS-7D-01','每周六出发','2-10人','中文导游','/images/popular/Anchorage To Santiago.jpg',1,'2025-03-15 20:48:16','2025-03-15 20:48:16','霍巴特出发，多地徒步路线','精品团队,徒步','霍巴特国际机场','250.00',NULL),(8,'塔斯马尼亚家庭欢乐之旅','家庭欢乐','专为家庭设计的塔斯马尼亚欢乐之旅','1300.00','1170.00',10,'5天4晚',5,4,'4.7',53,'TAS-5D-03','每周二、六出发','2-16人','中文导游','/images/popular/LA Explorer.jpg',1,'2025-03-15 20:48:16','2025-03-15 20:48:16','霍巴特出发，适合家庭的行程','精品团队,家庭友好','霍巴特国际机场','200.00',NULL),(9,'塔斯马尼亚蜜月浪漫之旅','蜜月浪漫','为新婚夫妇定制的浪漫塔斯马尼亚之旅','1800.00','1620.00',10,'6天5晚',6,5,'4.9',38,'TAS-6D-02','每周一、四、日出发','2-8人','中文导游','/images/tour/bali-1.png',1,'2025-03-15 20:48:16','2025-03-15 20:48:16','霍巴特出发，环岛浪漫景点','精品团队,浪漫之旅','霍巴特国际机场','220.00',NULL);

/*Table structure for table `guides` */

DROP TABLE IF EXISTS `guides`;

CREATE TABLE `guides` (
  `guide_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `bio` text,
  `experience_years` int DEFAULT NULL,
  `languages` varchar(255) DEFAULT NULL,
  `certification` varchar(255) DEFAULT NULL,
  `hourly_rate` decimal(10,2) DEFAULT NULL,
  `daily_rate` decimal(10,2) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`guide_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `guides` */

/*Table structure for table `itinerary_activities` */

DROP TABLE IF EXISTS `itinerary_activities`;

CREATE TABLE `itinerary_activities` (
  `activity_id` int NOT NULL AUTO_INCREMENT,
  `itinerary_id` int DEFAULT NULL,
  `activity_name` varchar(100) NOT NULL,
  PRIMARY KEY (`activity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `itinerary_activities` */

insert  into `itinerary_activities`(`activity_id`,`itinerary_id`,`activity_name`) values (1,1,'萨拉曼卡市场'),(2,1,'电池角历史区'),(3,1,'威灵顿山观景'),(4,2,'MONA现代艺术博物馆'),(5,2,'里士满历史小镇'),(6,2,'葡萄酒品鉴'),(7,3,'酒杯湾徒步'),(8,3,'菲欣纳国家公园'),(9,3,'夜间野生动物观赏'),(10,4,'摇篮山徒步'),(11,4,'多芬湖'),(12,4,'企鹅归巢观赏'),(13,5,'布鲁尼岛环岛游'),(14,5,'冒险湾'),(15,5,'布鲁尼岛灯塔');

/*Table structure for table `payments` */

DROP TABLE IF EXISTS `payments`;

CREATE TABLE `payments` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `booking_id` int DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `payment_method` varchar(50) DEFAULT NULL,
  `transaction_id` varchar(100) DEFAULT NULL,
  `status` enum('pending','completed','failed','refunded') DEFAULT 'pending',
  PRIMARY KEY (`payment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `payments` */

insert  into `payments`(`payment_id`,`booking_id`,`amount`,`payment_date`,`payment_method`,`transaction_id`,`status`) values (1,1,'2160.00','2023-10-15 14:35:00','credit_card','TXN123456789','completed'),(2,2,'2247.50','2023-10-16 10:20:00','credit_card','TXN123456790','completed'),(3,3,'480.00','2023-10-17 09:50:00','paypal','TXN123456791','completed'),(4,4,'300.00','2023-10-18 16:25:00','credit_card','TXN123456792','completed'),(5,5,'1350.00','2023-10-19 11:35:00','credit_card','TXN123456793','completed');

/*Table structure for table `regions` */

DROP TABLE IF EXISTS `regions`;

CREATE TABLE `regions` (
  `region_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text,
  `image_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`region_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `regions` */

insert  into `regions`(`region_id`,`name`,`description`,`image_url`) values (1,'塔斯马尼亚北部','塔斯马尼亚北部地区，包含摇篮山等著名景点',NULL),(2,'塔斯马尼亚南部','塔斯马尼亚南部地区，风景优美',NULL),(3,'塔斯马尼亚东部','塔斯马尼亚东部地区，包含酒杯湾等著名景点',NULL),(4,'塔斯马尼亚西部','塔斯马尼亚西部地区，原始自然风光',NULL),(5,'塔斯马尼亚中部','塔斯马尼亚中部地区，包含多个自然保护区',NULL),(6,'塔斯马尼亚东南部','塔斯马尼亚东南部地区，包含布鲁尼岛等景点',NULL),(7,'霍巴特','塔斯马尼亚首府，历史文化名城',NULL);

/*Table structure for table `reviews` */

DROP TABLE IF EXISTS `reviews`;

CREATE TABLE `reviews` (
  `review_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `tour_type` enum('day_tour','group_tour') NOT NULL,
  `tour_id` int NOT NULL,
  `rating` decimal(2,1) NOT NULL,
  `comment` text,
  `review_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_approved` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`review_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `reviews` */

insert  into `reviews`(`review_id`,`user_id`,`tour_type`,`tour_id`,`rating`,`comment`,`review_date`,`is_approved`) values (1,1,'group_tour',1,'4.8','非常棒的旅行体验！导游专业知识丰富，行程安排合理，住宿舒适。特别喜欢摇篮山的徒步和布鲁尼岛的企鹅观赏。强烈推荐给所有想要深入了解塔斯马尼亚的朋友。','2023-10-20 15:30:00',1),(2,2,'group_tour',3,'4.5','东海岸的风景非常美丽，海滩干净，海水清澈。导游服务周到，酒店也很舒适。唯一的小缺点是有一天的行程有点赶。总体来说非常值得推荐！','2023-10-21 09:45:00',1),(3,3,'day_tour',1,'5.0','摇篮山的风景太震撼了！导游非常专业，讲解详细，让我们了解了很多关于当地生态和历史的知识。徒步路线适中，不会太累。强烈推荐！','2023-10-22 16:20:00',1),(4,4,'day_tour',2,'4.7','酒杯湾真的很美，沙滩洁白，海水蔚蓝。导游安排了足够的自由活动时间让我们拍照和游泳。午餐的海鲜也很新鲜。非常愉快的一天！','2023-10-23 11:30:00',1),(5,5,'group_tour',4,'4.9','这次美食之旅太棒了！品尝了很多当地特色美食和葡萄酒，每一餐都是惊喜。导游对美食和葡萄酒的知识非常丰富，酒店也很有特色。完美的蜜月旅行！','2023-10-24 14:15:00',1);

/*Table structure for table `session_tokens` */

DROP TABLE IF EXISTS `session_tokens`;

CREATE TABLE `session_tokens` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `token` varchar(255) NOT NULL,
  `user_type` enum('regular','agent') NOT NULL,
  `agent_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `expires_at` timestamp NULL DEFAULT NULL,
  `last_active_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `session_tokens` */

/*Table structure for table `suitable_for` */

DROP TABLE IF EXISTS `suitable_for`;

CREATE TABLE `suitable_for` (
  `suitable_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`suitable_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `suitable_for` */

insert  into `suitable_for`(`suitable_id`,`name`,`description`) values (1,'家庭','适合家庭出游，有儿童友好活动'),(2,'情侣','适合情侣共度浪漫时光'),(3,'老年人','行程轻松，适合老年人'),(4,'团体','适合团体活动和大型团队');

/*Table structure for table `tour_exclusions` */

DROP TABLE IF EXISTS `tour_exclusions`;

CREATE TABLE `tour_exclusions` (
  `exclusion_id` int NOT NULL AUTO_INCREMENT,
  `group_tour_id` int DEFAULT NULL,
  `description` text NOT NULL,
  PRIMARY KEY (`exclusion_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `tour_exclusions` */

insert  into `tour_exclusions`(`exclusion_id`,`group_tour_id`,`description`) values (1,1,'国际/国内航班机票'),(2,1,'个人消费及小费'),(3,1,'行程外自选活动'),(4,1,'酒店单房差'),(5,1,'签证费用'),(6,1,'不可抗力因素导致的额外费用'),(7,1,'未明确列入包含项目的其他费用');

/*Table structure for table `tour_faqs` */

DROP TABLE IF EXISTS `tour_faqs`;

CREATE TABLE `tour_faqs` (
  `faq_id` int NOT NULL AUTO_INCREMENT,
  `group_tour_id` int DEFAULT NULL,
  `question` text NOT NULL,
  `answer` text NOT NULL,
  PRIMARY KEY (`faq_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `tour_faqs` */

insert  into `tour_faqs`(`faq_id`,`group_tour_id`,`question`,`answer`) values (1,1,'这个行程适合带儿童参加吗？','是的，此行程设计适合各年龄段游客，包括儿童。徒步活动强度较低，且有替代选项。请在预订时告知儿童年龄，我们可以提供相应的安排。'),(2,1,'行程中会有自由活动时间吗？','是的，行程中安排了适当的自由活动时间，特别是在霍巴特和菲欣纳半岛。您可以根据个人兴趣探索当地，导游会提供建议。'),(3,1,'需要提前多久预订？','建议至少提前30天预订，特别是在旅游旺季（12月至次年2月）。这样可以确保酒店住宿和活动的可用性。'),(4,1,'天气情况如何？需要带什么衣物？','塔斯马尼亚气候多变，即使在夏季也可能较凉。建议携带分层衣物、防水外套、舒适徒步鞋、帽子和防晒霜。我们会在出发前提供详细的装备清单。');

/*Table structure for table `tour_highlights` */

DROP TABLE IF EXISTS `tour_highlights`;

CREATE TABLE `tour_highlights` (
  `highlight_id` int NOT NULL AUTO_INCREMENT,
  `group_tour_id` int DEFAULT NULL,
  `description` text NOT NULL,
  PRIMARY KEY (`highlight_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `tour_highlights` */

insert  into `tour_highlights`(`highlight_id`,`group_tour_id`,`description`) values (1,1,'探索霍巴特历史街区，参观萨拉曼卡市场和MONA现代艺术博物馆'),(2,1,'游览世界著名的酒杯湾，欣赏绝美海滩和蔚蓝海水'),(3,1,'深入摇篮山国家公园，体验原始森林和高山湖泊的壮丽景色'),(4,1,'乘船游览布鲁尼岛，近距离观赏野生动物和壮观的海岸线'),(5,1,'品尝塔斯马尼亚特色美食，包括新鲜海鲜、当地葡萄酒和奶酪'),(6,1,'入住精选舒适酒店，享受贴心服务和便捷位置');

/*Table structure for table `tour_images` */

DROP TABLE IF EXISTS `tour_images`;

CREATE TABLE `tour_images` (
  `image_id` int NOT NULL AUTO_INCREMENT,
  `group_tour_id` int DEFAULT NULL,
  `image_url` varchar(255) NOT NULL,
  `thumbnail_url` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`image_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `tour_images` */

insert  into `tour_images`(`image_id`,`group_tour_id`,`image_url`,`thumbnail_url`,`description`) values (1,1,'/images/new/1.jpg','/images/new/1.jpg','霍巴特市区全景'),(2,1,'/images/new/2.jpg','/images/new/2.jpg','菲欣纳半岛海岸线'),(3,1,'/images/new/3.jpg','/images/new/3.jpg','世界著名的酒杯湾'),(4,1,'/images/new/4.jpg','/images/new/4.jpg','摇篮山国家公园'),(5,1,'/images/new/5.jpg','/images/new/5.jpg','布鲁尼岛灯塔'),(6,1,'/images/new/6.jpg','/images/new/6.jpg','塔斯马尼亚特色美食'),(7,1,'/images/new/7.jpg','/images/new/7.jpg','野生袋鼠'),(8,1,'/images/new/8.jpg','/images/new/8.jpg','MONA现代艺术博物馆');

/*Table structure for table `tour_inclusions` */

DROP TABLE IF EXISTS `tour_inclusions`;

CREATE TABLE `tour_inclusions` (
  `inclusion_id` int NOT NULL AUTO_INCREMENT,
  `group_tour_id` int DEFAULT NULL,
  `description` text NOT NULL,
  PRIMARY KEY (`inclusion_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `tour_inclusions` */

insert  into `tour_inclusions`(`inclusion_id`,`group_tour_id`,`description`) values (1,1,'4晚精选酒店住宿（双人间）'),(2,1,'专业中文导游全程陪同'),(3,1,'行程中注明的餐食（4早4午4晚）'),(4,1,'空调旅游巴士及专业司机'),(5,1,'所有景点门票及活动费用'),(6,1,'布鲁尼岛往返渡轮票'),(7,1,'机场-酒店接送服务'),(8,1,'每人每天一瓶矿泉水'),(9,1,'旅行保险');

/*Table structure for table `tour_itinerary` */

DROP TABLE IF EXISTS `tour_itinerary`;

CREATE TABLE `tour_itinerary` (
  `itinerary_id` int NOT NULL AUTO_INCREMENT,
  `group_tour_id` int DEFAULT NULL,
  `day_number` int NOT NULL,
  `title` varchar(100) NOT NULL,
  `description` text,
  `meals` varchar(100) DEFAULT NULL,
  `accommodation` varchar(100) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`itinerary_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `tour_itinerary` */

insert  into `tour_itinerary`(`itinerary_id`,`group_tour_id`,`day_number`,`title`,`description`,`meals`,`accommodation`,`image_url`) values (1,1,1,'抵达霍巴特 - 市区观光','抵达霍巴特国际机场后，我们的专业导游将在机场迎接您，并送您前往市区酒店办理入住。稍作休息后，我们将开始霍巴特市区半日游，参观历史悠久的萨拉曼卡市场，这里有各种手工艺品、当地美食和新鲜农产品。随后游览电池角历史区，了解塔斯马尼亚早期殖民历史。傍晚时分登上威灵顿山，俯瞰整个霍巴特城市和德文特河的壮丽景色，欣赏日落美景。晚餐在当地特色餐厅享用塔斯马尼亚海鲜盛宴。','晚餐','霍巴特格兰德酒店或同级','/images/new/1.jpg'),(2,1,2,'霍巴特 - MONA博物馆 - 菲欣纳半岛','早餐后，乘坐特色渡轮前往MONA现代艺术博物馆，这是澳大利亚最大的私人博物馆，以其前卫和有争议的艺术作品而闻名。午餐后驱车前往菲欣纳半岛，途经里士满历史小镇，参观澳洲最古老的石桥。抵达菲欣纳半岛后，入住海景酒店。傍晚时分前往当地葡萄酒庄园，品尝塔斯马尼亚特色葡萄酒和奶酪拼盘，欣赏美丽的葡萄园景色。','早餐、午餐、晚餐','菲欣纳海湾度假酒店或同级','/images/new/2.jpg'),(3,1,3,'菲欣纳半岛 - 酒杯湾 - 摇篮山','早餐后，前往世界著名的酒杯湾，这里有完美的新月形海滩和清澈的蓝色海水。您可以选择徒步前往酒杯湾观景台，欣赏标志性的全景视图，或者下到海滩漫步，感受细腻的白沙和清澈的海水。午餐后，参观菲欣纳国家公园内的灯塔和塔斯曼拱门等自然奇观。下午驱车前往摇篮山国家公园，沿途欣赏塔斯马尼亚中部的田园风光。抵达后入住国家公园附近的山林小屋，晚餐后可以参加夜间野生动物观赏活动，寻找塔斯马尼亚特有的野生动物。','早餐、午餐、晚餐','摇篮山山林小屋或同级','/images/new/3.jpg'),(4,1,4,'摇篮山国家公园 - 布鲁尼岛','早餐后，深入探索摇篮山国家公园，这里是世界遗产区域，拥有壮观的山脉和原始森林。我们将进行适合各年龄段的轻松徒步，欣赏多芬湖的美丽景色，了解当地独特的生态系统。午餐后，驱车返回霍巴特，然后乘坐渡轮前往布鲁尼岛。抵达后入住岛上特色住宿，晚餐后可以参加企鹅归巢观赏活动，近距离观看小企鹅从海上归来的可爱场景。','早餐、午餐、晚餐','布鲁尼岛海景度假村或同级','/images/new/4.jpg'),(5,1,5,'布鲁尼岛 - 霍巴特','早餐后，参加布鲁尼岛环岛游，探访岛上的自然景观和历史遗迹，包括布鲁尼岛灯塔、冒险湾和企鹅栖息地。午餐品尝当地特色海鲜。下午乘坐渡轮返回霍巴特，根据您的航班时间，我们的导游将送您前往霍巴特国际机场，结束愉快的塔斯马尼亚之旅。','早餐、午餐','无','/images/new/5.jpg');

/*Table structure for table `tour_tips` */

DROP TABLE IF EXISTS `tour_tips`;

CREATE TABLE `tour_tips` (
  `tip_id` int NOT NULL AUTO_INCREMENT,
  `group_tour_id` int DEFAULT NULL,
  `description` text NOT NULL,
  PRIMARY KEY (`tip_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `tour_tips` */

insert  into `tour_tips`(`tip_id`,`group_tour_id`,`description`) values (1,1,'塔斯马尼亚天气多变，建议随身携带防晒霜和雨具'),(2,1,'摇篮山徒步需穿着舒适的徒步鞋和分层衣物'),(3,1,'布鲁尼岛观赏企鹅时需保持安静，不使用闪光灯'),(4,1,'行程中有机会购买当地特产，如薰衣草制品、蜂蜜和手工艺品'),(5,1,'澳大利亚插座为V型，请携带转换插头');

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `user_type` enum('regular','agent') DEFAULT 'regular' COMMENT '用户类型：regular-普通用户，agent-代理商',
  `agent_id` int DEFAULT NULL COMMENT '关联的代理商ID',
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` enum('customer','admin','guide') DEFAULT 'customer',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_agent_id` (`agent_id`),
  CONSTRAINT `fk_user_agent` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_users_agent` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `users` */

insert  into `users`(`user_id`,`username`,`password`,`user_type`,`agent_id`,`first_name`,`last_name`,`email`,`phone`,`role`,`created_at`,`updated_at`) values (1,'johndoe','$2a$10$XFE.tQgTCfwVVD6WqOVB6.Vt7QLWy9.W9GJ9A.VtEMF8EaJPm7UQy','regular',NULL,'John','Doe','john.doe@example.com','+61412345678','customer','2025-03-15 20:52:17','2025-03-15 20:52:17'),(2,'janedoe','$2a$10$XFE.tQgTCfwVVD6WqOVB6.Vt7QLWy9.W9GJ9A.VtEMF8EaJPm7UQy','regular',NULL,'Jane','Doe','jane.doe@example.com','+61412345679','customer','2025-03-15 20:52:17','2025-03-15 20:52:17'),(3,'bobsmith','$2a$10$XFE.tQgTCfwVVD6WqOVB6.Vt7QLWy9.W9GJ9A.VtEMF8EaJPm7UQy','regular',NULL,'Bob','Smith','bob.smith@example.com','+61412345680','customer','2025-03-15 20:52:17','2025-03-15 20:52:17'),(4,'alicejones','$2a$10$XFE.tQgTCfwVVD6WqOVB6.Vt7QLWy9.W9GJ9A.VtEMF8EaJPm7UQy','regular',NULL,'Alice','Jones','alice.jones@example.com','+61412345681','customer','2025-03-15 20:52:17','2025-03-15 20:52:17'),(5,'mikebrown','$2a$10$XFE.tQgTCfwVVD6WqOVB6.Vt7QLWy9.W9GJ9A.VtEMF8EaJPm7UQy','regular',NULL,'Mike','Brown','mike.brown@example.com','+61412345682','customer','2025-03-15 20:52:17','2025-03-15 20:52:17'),(6,'user1','123456','regular',NULL,'测试','用户','user1@example.com','+61412345678','customer','2025-03-18 23:24:22','2025-03-18 23:24:22'),(8,'agent1','e10adc3949ba59abbe56e057f20f883e','agent',1,'张三','塔斯旅游','agent1@example.com','13800138001','customer','2025-03-19 10:26:59','2025-03-19 10:26:59'),(9,'agent2','e10adc3949ba59abbe56e057f20f883e','agent',2,'李四','悉尼旅游','agent2@example.com','13800138002','customer','2025-03-19 10:26:59','2025-03-19 10:26:59'),(10,'agent3','e10adc3949ba59abbe56e057f20f883e','agent',3,'王五','墨尔本旅游','agent3@example.com','13800138003','customer','2025-03-19 10:26:59','2025-03-19 10:26:59');

/*Table structure for table `vehicle_driver` */

DROP TABLE IF EXISTS `vehicle_driver`;

CREATE TABLE `vehicle_driver` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_id` bigint NOT NULL,
  `employee_id` bigint NOT NULL,
  `is_primary` int DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `vehicle_driver` */

insert  into `vehicle_driver`(`id`,`vehicle_id`,`employee_id`,`is_primary`,`create_time`,`update_time`) values (17,1,7,0,'2025-03-21 11:02:28','2025-03-21 11:02:28'),(18,4,5,0,'2025-03-21 11:03:24','2025-03-21 11:03:24');

/*Table structure for table `vehicles` */

DROP TABLE IF EXISTS `vehicles`;

CREATE TABLE `vehicles` (
  `vehicle_id` bigint NOT NULL AUTO_INCREMENT,
  `vehicle_type` varchar(50) DEFAULT NULL,
  `license_plate` varchar(20) NOT NULL,
  `rego_expiry_date` date DEFAULT NULL,
  `inspection_due_date` date DEFAULT NULL,
  `status` int DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `max_drivers` int DEFAULT '3',
  `location` varchar(100) DEFAULT NULL,
  `seat_count` int DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`vehicle_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Data for the table `vehicles` */

insert  into `vehicles`(`vehicle_id`,`vehicle_type`,`license_plate`,`rego_expiry_date`,`inspection_due_date`,`status`,`notes`,`max_drivers`,`location`,`seat_count`,`create_time`,`update_time`) values (1,'SUV','A12345','2025-12-25','2025-06-30',3,'新车',1,'12 rodway court,kingston',5,'2025-03-11 17:46:37',NULL),(2,'轿车','B67890','2025-05-31','2025-12-20',1,'保养良好',2,'深圳',4,'2025-03-11 17:46:37',NULL),(3,'大巴','C11223','2024-11-30','2024-05-31',1,'适合长途',3,'珠海',20,'2025-03-11 17:46:37','2025-03-11 17:46:37'),(4,'货车','D44556','2027-10-02','2026-04-03',2,'载重5吨',3,'佛山',2,'2025-03-11 17:46:37',NULL),(5,'SUV','E77889','2024-09-30','2024-03-31',1,'四驱',3,'东莞',5,'2025-03-11 17:46:37','2025-03-11 17:46:37'),(6,'轿车','F99001','2024-08-31','2024-02-28',1,'省油',3,'中山',4,'2025-03-11 17:46:37','2025-03-11 17:46:37'),(7,'大巴','G22334','2024-07-31','2024-01-31',1,'豪华大巴',3,'惠州',20,'2025-03-11 17:46:37','2025-03-11 17:46:37'),(8,'货车','H55667','2024-06-30','2023-12-31',1,'载重10吨',3,'江门',2,'2025-03-11 17:46:37','2025-03-11 17:46:37'),(9,'小型巴士','46xt','2025-03-24','2025-03-18',1,NULL,2,'12 rodway',25,NULL,NULL);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
