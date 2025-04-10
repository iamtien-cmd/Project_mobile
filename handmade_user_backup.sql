-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: handmade
-- ------------------------------------------------------
-- Server version	5.5.5-10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `address_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `house_number` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`address_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `cart_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`cart_id`),
  UNIQUE KEY `UK9emlp6m95v5er2bcqkjsw48he` (`user_id`),
  CONSTRAINT `FKl70asp4l4w0jmbm1tqyofho4o` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_item`
--

DROP TABLE IF EXISTS `cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_item` (
  `cart_item_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `quantity` int(11) NOT NULL,
  `cart_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  PRIMARY KEY (`cart_item_id`),
  KEY `FK1uobyhgl1wvgt1jpccia8xxs3` (`cart_id`),
  KEY `FKjcyd5wv4igqnw413rgxbfu4nv` (`product_id`),
  CONSTRAINT `FK1uobyhgl1wvgt1jpccia8xxs3` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`cart_id`),
  CONSTRAINT `FKjcyd5wv4igqnw413rgxbfu4nv` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_item`
--

LOCK TABLES `cart_item` WRITE;
/*!40000 ALTER TABLE `cart_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,NULL),(2,NULL),(3,NULL),(4,NULL),(101,'Trang sức'),(102,'Trang trí nhà cửa'),(103,'Giấy & Thiệp'),(104,'Đèn & Nến'),(105,'Túi & Ví');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `comment_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `rating` int(11) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`comment_id`),
  KEY `FKm1rmnfcvq5mk26li4lit88pc5` (`product_id`),
  KEY `FK8kcum44fvpupyw6f5baccx25c` (`user_id`),
  CONSTRAINT `FK8kcum44fvpupyw6f5baccx25c` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `FKm1rmnfcvq5mk26li4lit88pc5` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite` (
  `favorite_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `favorite_name` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`favorite_id`),
  KEY `FKh3f2dg11ibnht4fvnmx60jcif` (`user_id`),
  CONSTRAINT `FKh3f2dg11ibnht4fvnmx60jcif` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite`
--

LOCK TABLES `favorite` WRITE;
/*!40000 ALTER TABLE `favorite` DISABLE KEYS */;
INSERT INTO `favorite` VALUES (1,NULL,NULL),(2,NULL,NULL),(3,NULL,NULL),(4,NULL,NULL);
/*!40000 ALTER TABLE `favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_line`
--

DROP TABLE IF EXISTS `order_line`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_line` (
  `order_line_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `quantity` int(11) NOT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `product_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`order_line_id`),
  KEY `FKk9f9t1tmkbq5w27u8rrjbxxg6` (`order_id`),
  KEY `FKpf904tci8garypkvm32cqupye` (`product_id`),
  CONSTRAINT `FKk9f9t1tmkbq5w27u8rrjbxxg6` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`),
  CONSTRAINT `FKpf904tci8garypkvm32cqupye` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_line`
--

LOCK TABLES `order_line` WRITE;
/*!40000 ALTER TABLE `order_line` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_line` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_date` date DEFAULT NULL,
  `predict_receive_date` date DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','CONFIRMED','PENDING','REFUNDED','SHIPPING') DEFAULT NULL,
  `total_price` double NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`order_id`),
  KEY `FKel9kyl84ego2otj2accfd8mr7` (`user_id`),
  CONSTRAINT `FKel9kyl84ego2otj2accfd8mr7` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `product_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `price` double NOT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  `favorite_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`product_id`),
  KEY `FK1mtsbur82frn64de7balymq9s` (`category_id`),
  KEY `FK1awjd8t5th5meqd80gqf1vbyq` (`favorite_id`),
  CONSTRAINT `FK1awjd8t5th5meqd80gqf1vbyq` FOREIGN KEY (`favorite_id`) REFERENCES `favorite` (`favorite_id`),
  CONSTRAINT `FK1mtsbur82frn64de7balymq9s` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1051 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1001,'Vòng tay đá thạch anh hồng handmade','vt-da-hong.jpg','Vòng tay đá tự nhiên',150000,101,NULL),(1002,'Vòng tay gỗ khắc tên theo yêu cầu','vt-go-khac.jpg','Vòng tay gỗ khắc chữ',120000,101,NULL),(1003,'Dây chuyền ngọc trai biển xanh','dc-ngoc-trai.jpg','Dây chuyền ngọc trai',250000,101,NULL),(1004,'Khung ảnh gỗ tái chế handmade','khung-anh.jpg','Khung ảnh gỗ',130000,102,NULL),(1005,'Tranh thêu tay chủ đề thiên nhiên','tranh-theu.jpg','Tranh thêu tay',300000,102,NULL),(1006,'Thiệp pop-up 3D handmade mừng sinh nhật','thiep-popup.jpg','Thiệp pop-up',50000,103,NULL),(1007,'Sáp thơm cắm que mùi oải hương','sap-thom.jpg','Sáp thơm cắm que',80000,104,NULL),(1008,'Nến thơm handmade hình hoa hồng','nen-thom.jpg','Nến thơm thủ công',90000,104,NULL),(1009,'Ví da bò khâu tay nhỏ gọn','vi-da.jpg','Ví da handmade',200000,105,NULL),(1010,'Túi vải thổ cẩm đeo chéo','tui-tho-cam.jpg','Túi vải thổ cẩm',180000,105,NULL),(1011,'Khung ảnh gỗ để bàn','khung-ban.jpg','Khung ảnh gỗ nhỏ',100000,102,NULL),(1012,'Móc khóa resin hình hoa cúc','moc-khoa.jpg','Móc khóa resin',60000,103,NULL),(1013,'Đèn ngủ đất sét vẽ tay','den-ngu.jpg','Đèn ngủ đất sét',220000,104,NULL),(1014,'Ốp lưng gỗ khắc laser','op-lung.jpg','Ốp lưng điện thoại',170000,105,NULL),(1015,'Sổ tay bìa da handmade A5','so-tay.jpg','Sổ tay bìa da',140000,103,NULL),(1016,'Lót cốc thêu tay họa tiết hoa','lot-coc.jpg','Lót cốc thêu tay',40000,103,NULL),(1017,'Túi canvas vẽ tay nghệ thuật','tui-canvas.jpg','Túi canvas vẽ tay',160000,105,NULL),(1018,'Vòng cổ makrame tông xanh','vong-co.jpg','Vòng cổ makrame',120000,101,NULL),(1019,'Bình gốm sứ vẽ tay','binh-gom.jpg','Bình gốm sứ',260000,102,NULL),(1020,'Thảm chùi chân len đan tay','tham-chan.jpg','Thảm chùi chân',110000,105,NULL),(1021,'Gối tựa lưng thêu tay','goi-lung.jpg','Gối tựa lưng',150000,103,NULL),(1022,'Dây chuyền gỗ khắc hình','dc-go.jpg','Mặt dây chuyền gỗ',130000,101,NULL),(1023,'Khăn choàng len dệt thủ công','khan-len.jpg','Khăn choàng len',190000,105,NULL),(1024,'Ví cầm tay vải hoa văn','vi-vai.jpg','Ví cầm tay vải',90000,105,NULL),(1025,'Khung gương gỗ vintage','guong.jpg','Khung gương vintage',240000,102,NULL),(1026,'Đồng hồ gỗ treo tường handmade','dong-ho.jpg','Đồng hồ gỗ',320000,102,NULL),(1027,'Thiệp cưới handmade sang trọng','thiep-cuoi.jpg','Thiệp cưới',70000,103,NULL),(1028,'Bình hoa đất nung mộc mạc','binh-dat.jpg','Bình hoa đất nung',210000,102,NULL),(1029,'Tượng đất sét mini','tuong.jpg','Tượng đất sét',95000,104,NULL),(1030,'Túi rút dây vải thô','tui-rut.jpg','Túi rút dây',80000,105,NULL),(1031,'Vòng tay charm mix hạt gỗ','vt-charm.jpg','Vòng tay charm',110000,101,NULL),(1032,'Móc khóa vải thêu tay','moc-vai.jpg','Móc khóa vải',50000,103,NULL),(1033,'Đèn lồng giấy handmade','den-long.jpg','Đèn lồng giấy',120000,104,NULL),(1034,'Tranh canvas in tay','tranh-canvas.jpg','Tranh canvas',280000,102,NULL),(1035,'Bình giữ nhiệt vẽ tay','binh-giu-nhiet.jpg','Bình giữ nhiệt',230000,104,NULL),(1036,'Khung ảnh macrame treo tường','macrame.jpg','Khung ảnh macrame',220000,102,NULL),(1037,'Túi tote canvas vẽ tay','tui-tote.jpg','Túi tote vẽ tay',150000,105,NULL),(1038,'Lịch gỗ handmade','lich-go.jpg','Lịch gỗ treo tường',200000,102,NULL),(1039,'Tượng gỗ điêu khắc nhỏ','tuong-go.jpg','Tượng gỗ mini',180000,102,NULL),(1040,'Vòng tay len móc tay','vt-len.jpg','Vòng tay len',90000,101,NULL),(1041,'Mặt nạ giấy hoa văn','mat-na.jpg','Mặt nạ giấy handmade',60000,104,NULL),(1042,'Sách tay vẽ A5','sach-ve.jpg','Sách tay vẽ',80000,103,NULL),(1043,'Băng đô vải thắt nơ','bang-do.jpg','Băng đô vải',50000,105,NULL),(1044,'Bông tai resin hình giọt nước','bong-tai.jpg','Bông tai resin',70000,103,NULL),(1045,'Bông tai thổ cẩm nhiều màu','bong-tai2.jpg','Bông tai thổ cẩm',75000,103,NULL),(1046,'Ví da bò mini','vi-da-mini.jpg','Ví da nhỏ',160000,105,NULL),(1047,'Khung ảnh kim loại mỏng','khung-kim.jpg','Khung ảnh kim loại',210000,102,NULL),(1048,'Thiệp Giáng Sinh 3D','thiep-giang-sinh.jpg','Thiệp Giáng Sinh',60000,103,NULL),(1049,'Túi vải tái chế handmade','tui-tai-che.jpg','Túi vải tái chế',140000,105,NULL),(1050,'Móc khóa pha lê handmade','moc-pha-le.jpg','Móc khóa pha lê',80000,103,NULL);
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `otp_code` varchar(255) DEFAULT NULL,
  `otp_expiration` datetime(6) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `type` enum('New','Regular','VIP') DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UKob8kqyqqgmefl0aco34akdtpe` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,_binary '','123','Nguyễn Minh Hòa','123456','2025-04-10 23:59:59.000000','$2a$10$msgtNZIEoZB7GV7Hy7tTiu9QceyhujbGrL8vzrrE3541yIlCM5OXm','New'),(2,_binary '','hoamai@example.com','Trần Hoa Mai','123456','2025-04-10 23:59:59.000000','$2a$10$msgtNZIEoZB7GV7Hy7tTiu9QceyhujbGrL8vzrrE3541yIlCM5OXm','New'),(3,_binary '','thienan@example.com','Lê Thiên Ân','123456','2025-04-10 23:59:59.000000','$2a$10$msgtNZIEoZB7GV7Hy7tTiu9QceyhujbGrL8vzrrE3541yIlCM5OXm','New'),(4,_binary '','tuyettrinh@example.com','Phạm Tuyết Trinh','123456','2025-04-10 23:59:59.000000','$2a$10$msgtNZIEoZB7GV7Hy7tTiu9QceyhujbGrL8vzrrE3541yIlCM5OXm','New'),(5,_binary '','adminshop@example.com','Admin Handmade','123456','2025-04-10 23:59:59.000000','$2a$10$msgtNZIEoZB7GV7Hy7tTiu9QceyhujbGrL8vzrrE3541yIlCM5OXm','New');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_address`
--

DROP TABLE IF EXISTS `user_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_address` (
  `user_id` bigint(20) NOT NULL,
  `address_id` bigint(20) NOT NULL,
  KEY `FKdaaxogn1ss81gkcsdn05wi6jp` (`address_id`),
  KEY `FKk2ox3w9jm7yd6v1m5f68xibry` (`user_id`),
  CONSTRAINT `FKdaaxogn1ss81gkcsdn05wi6jp` FOREIGN KEY (`address_id`) REFERENCES `address` (`address_id`),
  CONSTRAINT `FKk2ox3w9jm7yd6v1m5f68xibry` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_address`
--

LOCK TABLES `user_address` WRITE;
/*!40000 ALTER TABLE `user_address` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_address` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-10 17:15:24
