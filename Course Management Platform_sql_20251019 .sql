/*
 Navicat Premium Dump SQL

 Source Server         : dockermysql
 Source Server Type    : MySQL
 Source Server Version : 80406 (8.4.6)
 Source Host           : localhost:3306
 Source Schema         : course_management_platform

 Target Server Type    : MySQL
 Target Server Version : 80406 (8.4.6)
 File Encoding         : 65001

 Date: 16/12/2025 20:26:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for assignment_submissions
-- ----------------------------
DROP TABLE IF EXISTS `assignment_submissions`;
CREATE TABLE `assignment_submissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assignment_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `submission_text` text COLLATE utf8mb4_unicode_ci,
  `attachment_path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `submitted_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `score` decimal(5,2) DEFAULT NULL,
  `feedback` text COLLATE utf8mb4_unicode_ci,
  `status` enum('SUBMITTED','GRADED','LATE') COLLATE utf8mb4_unicode_ci DEFAULT 'SUBMITTED',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_submission` (`assignment_id`,`student_id`),
  KEY `student_id` (`student_id`),
  CONSTRAINT `assignment_submissions_ibfk_1` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`),
  CONSTRAINT `assignment_submissions_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of assignment_submissions
-- ----------------------------
BEGIN;
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `submission_text`, `attachment_path`, `submitted_at`, `score`, `feedback`, `status`) VALUES (40, 30, 79, '', 'submission-files/30/79/51d45e2293e9484e854c0d3a46ee108b.docx', '2025-12-16 12:01:15', 90.00, '', 'GRADED');
COMMIT;

-- ----------------------------
-- Table structure for assignments
-- ----------------------------
DROP TABLE IF EXISTS `assignments`;
CREATE TABLE `assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `max_score` decimal(5,2) NOT NULL,
  `deadline` datetime NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `course_id` (`course_id`),
  CONSTRAINT `assignments_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of assignments
-- ----------------------------
BEGIN;
INSERT INTO `assignments` (`id`, `course_id`, `title`, `description`, `max_score`, `deadline`, `created_at`) VALUES (23, 32, '测试作业1', '', 100.00, '2025-12-18 11:12:00', NULL);
INSERT INTO `assignments` (`id`, `course_id`, `title`, `description`, `max_score`, `deadline`, `created_at`) VALUES (30, 33, '测试作业1', '', 100.00, '2025-12-17 00:00:00', NULL);
COMMIT;

-- ----------------------------
-- Table structure for course_resources
-- ----------------------------
DROP TABLE IF EXISTS `course_resources`;
CREATE TABLE `course_resources` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_size` bigint DEFAULT NULL,
  `uploader_id` bigint NOT NULL,
  `uploaded_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `download_count` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `course_id` (`course_id`),
  KEY `uploader_id` (`uploader_id`),
  CONSTRAINT `course_resources_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
  CONSTRAINT `course_resources_ibfk_2` FOREIGN KEY (`uploader_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of course_resources
-- ----------------------------
BEGIN;
INSERT INTO `course_resources` (`id`, `course_id`, `title`, `description`, `file_path`, `file_size`, `uploader_id`, `uploaded_at`, `download_count`) VALUES (2, 32, '测试附件1', 'assignment:23', 'course-files/32/3cbbd276c7104851bdbae9ad30008d9d.docx', 913566, 72, '2025-12-16 11:40:05', 1);
INSERT INTO `course_resources` (`id`, `course_id`, `title`, `description`, `file_path`, `file_size`, `uploader_id`, `uploaded_at`, `download_count`) VALUES (5, 32, 'ceshi1', NULL, 'course-files/32/8e53fa5ef0cf4be2a4eefffae03270d7.docx', 913566, 72, '2025-12-16 11:45:16', 0);
INSERT INTO `course_resources` (`id`, `course_id`, `title`, `description`, `file_path`, `file_size`, `uploader_id`, `uploaded_at`, `download_count`) VALUES (6, 32, '测试附件2', NULL, 'course-files/32/2fd7403e55ab415d90b1caf9a8d76a38.jpg', 2442445, 72, '2025-12-16 11:48:29', 0);
INSERT INTO `course_resources` (`id`, `course_id`, `title`, `description`, `file_path`, `file_size`, `uploader_id`, `uploaded_at`, `download_count`) VALUES (7, 33, '测试附件3', 'assignment:30', 'course-files/33/dddfa90a5fe74b57a715a2ed08f65437.pdf', 4071000, 72, '2025-12-16 11:48:43', 0);
COMMIT;

-- ----------------------------
-- Table structure for course_selections
-- ----------------------------
DROP TABLE IF EXISTS `course_selections`;
CREATE TABLE `course_selections` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `course_id` bigint NOT NULL,
  `selected_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('SELECTED','WITHDRAWN','COMPLETED') COLLATE utf8mb4_unicode_ci DEFAULT 'SELECTED',
  `final_score` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_selection` (`student_id`,`course_id`),
  KEY `course_id` (`course_id`),
  CONSTRAINT `course_selections_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `course_selections_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of course_selections
-- ----------------------------
BEGIN;
INSERT INTO `course_selections` (`id`, `student_id`, `course_id`, `selected_at`, `status`, `final_score`) VALUES (1, 79, 33, '2025-12-16 12:21:25', 'SELECTED', NULL);
INSERT INTO `course_selections` (`id`, `student_id`, `course_id`, `selected_at`, `status`, `final_score`) VALUES (2, 79, 32, '2025-12-16 12:21:40', 'SELECTED', NULL);
COMMIT;

-- ----------------------------
-- Table structure for courses
-- ----------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `course_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `credit` decimal(3,1) NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `teacher_id` bigint NOT NULL,
  `max_students` int DEFAULT '50',
  `current_students` int DEFAULT '0',
  `semester` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('OPEN','CLOSED','CANCELLED') COLLATE utf8mb4_unicode_ci DEFAULT 'OPEN',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `course_code` (`course_code`),
  KEY `teacher_id` (`teacher_id`),
  CONSTRAINT `courses_ibfk_1` FOREIGN KEY (`teacher_id`) REFERENCES `teachers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=122 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of courses
-- ----------------------------
BEGIN;
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (1, 'CS001', 'Java程序设计', 3.0, '学习Java语言基础、面向对象编程和Web开发', 1, 50, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (2, 'CS002', '数据库原理', 3.5, '学习关系数据库理论和SQL语言', 2, 45, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (3, 'CS003', 'Web前端开发', 2.5, '学习HTML、CSS、JavaScript等前端技术', 3, 40, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (4, 'CS004', '数据结构与算法', 4.0, '学习基本数据结构和常用算法', 4, 50, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (5, 'CS005', '操作系统', 3.5, '学习操作系统原理和实现', 5, 45, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (6, 'CS006', '计算机网络', 3.0, '学习网络协议和网络编程', 6, 50, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (7, 'CS007', '软件工程', 3.0, '学习软件开发流程和方法论', 7, 40, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (8, 'CS008', '人工智能基础', 3.0, '学习人工智能基本概念和算法', 8, 45, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (9, 'CS009', '机器学习', 3.5, '学习机器学习理论和实践', 9, 35, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (10, 'CS010', '计算机组成原理', 3.0, '学习计算机硬件组成和工作原理', 10, 50, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (11, 'CS011', '编译原理', 3.0, '学习编译器的设计和实现', 1, 30, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (12, 'CS012', '分布式系统', 3.0, '学习分布式系统理论和实践', 2, 35, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (13, 'CS013', '移动应用开发', 2.5, '学习Android或iOS应用开发', 3, 40, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (14, 'CS014', '云计算技术', 3.0, '学习云计算平台和技术', 4, 40, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (15, 'MA015', '高等数学', 4.0, '微积分、线性代数等数学基础', 5, 60, 0, '2023-2024-1', 'OPEN', '2025-11-02 11:12:17');
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (32, 'CS110', '测试课程', 2.0, NULL, 19, 2, 1, '2025-2026-1', 'OPEN', NULL);
INSERT INTO `courses` (`id`, `course_code`, `course_name`, `credit`, `description`, `teacher_id`, `max_students`, `current_students`, `semester`, `status`, `created_at`) VALUES (33, 'CS111', '测试课程2', 3.0, '\n', 19, 3, 1, '2025-2026-1', 'OPEN', NULL);
COMMIT;

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `publisher_id` bigint NOT NULL,
  `publish_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_important` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `course_id` (`course_id`),
  KEY `publisher_id` (`publisher_id`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
  CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`publisher_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of notifications
-- ----------------------------
BEGIN;
INSERT INTO `notifications` (`id`, `course_id`, `title`, `content`, `publisher_id`, `publish_time`, `is_important`) VALUES (1, 33, '选课成功通知', '学生张志伟已成功选修课程《测试课程2》，授课教师：张屹。', 72, NULL, 0);
INSERT INTO `notifications` (`id`, `course_id`, `title`, `content`, `publisher_id`, `publish_time`, `is_important`) VALUES (2, 32, '选课成功通知', '学生张志伟已成功选修课程《测试课程》，授课教师：张屹。', 72, NULL, 0);
INSERT INTO `notifications` (`id`, `course_id`, `title`, `content`, `publisher_id`, `publish_time`, `is_important`) VALUES (3, 12, '选课成功通知', '学生张志伟已成功选修课程《分布式系统》，授课教师：李副教授。', 3, NULL, 0);
INSERT INTO `notifications` (`id`, `course_id`, `title`, `content`, `publisher_id`, `publish_time`, `is_important`) VALUES (4, 12, '退课成功通知', '学生张志伟已退选课程《分布式系统》，授课教师：李副教授。', 3, NULL, 0);
INSERT INTO `notifications` (`id`, `course_id`, `title`, `content`, `publisher_id`, `publish_time`, `is_important`) VALUES (5, 32, 'hello', 'hello', 1, NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for students
-- ----------------------------
DROP TABLE IF EXISTS `students`;
CREATE TABLE `students` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `student_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `class_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `major` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enrollment_year` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `student_id` (`student_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `students_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of students
-- ----------------------------
BEGIN;
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (1, 12, '2023001001', '计算机2301', '计算机科学与技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (2, 13, '2023001002', '计算机2301', '计算机科学与技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (3, 14, '2023001003', '计算机2301', '计算机科学与技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (4, 15, '2023001004', '计算机2301', '计算机科学与技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (5, 16, '2023001005', '计算机2301', '计算机科学与技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (6, 17, '2023001006', '计算机2302', '计算机科学与技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (7, 18, '2023001007', '计算机2302', '计算机科学与技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (8, 19, '2023001008', '计算机2302', '计算机科学与技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (9, 20, '2023001009', '计算机2302', '计算机科学与技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (10, 21, '2023001010', '计算机2302', '计算机科学与技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (11, 22, '2023001011', '软件2301', '软件工程', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (12, 23, '2023001012', '软件2301', '软件工程', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (13, 24, '2023001013', '软件2301', '软件工程', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (14, 25, '2023001014', '软件2301', '软件工程', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (15, 26, '2023001015', '软件2301', '软件工程', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (16, 27, '2023001016', '软件2302', '软件工程', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (17, 28, '2023001017', '软件2302', '软件工程', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (18, 29, '2023001018', '软件2302', '软件工程', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (19, 30, '2023001019', '软件2302', '软件工程', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (20, 31, '2023001020', '软件2302', '软件工程', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (21, 32, '2023001021', '网安2301', '网络空间安全', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (22, 33, '2023001022', '网安2301', '网络空间安全', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (23, 34, '2023001023', '网安2301', '网络空间安全', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (24, 35, '2023001024', '网安2301', '网络空间安全', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (25, 36, '2023001025', '网安2301', '网络空间安全', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (26, 37, '2023001026', '网安2302', '网络空间安全', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (27, 38, '2023001027', '网安2302', '网络空间安全', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (28, 39, '2023001028', '网安2302', '网络空间安全', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (29, 40, '2023001029', '网安2302', '网络空间安全', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (30, 41, '2023001030', '网安2302', '网络空间安全', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (31, 42, '2023001031', '数据2301', '数据科学与大数据技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (32, 43, '2023001032', '数据2301', '数据科学与大数据技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (33, 44, '2023001033', '数据2301', '数据科学与大数据技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (34, 45, '2023001034', '数据2301', '数据科学与大数据技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (35, 46, '2023001035', '数据2301', '数据科学与大数据技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (36, 47, '2023001036', '数据2302', '数据科学与大数据技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (37, 48, '2023001037', '数据2302', '数据科学与大数据技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (38, 49, '2023001038', '数据2302', '数据科学与大数据技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (39, 50, '2023001039', '数据2302', '数据科学与大数据技术', 2023);
INSERT INTO `students` (`id`, `user_id`, `student_id`, `class_name`, `major`, `enrollment_year`) VALUES (79, 193, 'S193', NULL, NULL, 2016);
COMMIT;

-- ----------------------------
-- Table structure for teachers
-- ----------------------------
DROP TABLE IF EXISTS `teachers`;
CREATE TABLE `teachers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `teacher_id` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `department` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `office` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `teacher_id` (`teacher_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `teachers_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of teachers
-- ----------------------------
BEGIN;
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (1, 2, 'T001', '计算机科学与技术学院', '教授', 'A101');
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (2, 3, 'T002', '软件工程学院', '副教授', 'A102');
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (3, 4, 'T003', '网络空间安全学院', '讲师', 'A103');
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (4, 5, 'T004', '数据科学与工程学院', '教授', 'A104');
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (5, 6, 'T005', '人工智能学院', '副教授', 'A105');
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (6, 7, 'T006', '计算机科学与技术学院', '讲师', 'A106');
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (7, 8, 'T007', '软件工程学院', '教授', 'A107');
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (8, 9, 'T008', '网络空间安全学院', '副教授', 'A108');
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (9, 10, 'T009', '数据科学与工程学院', '讲师', 'A109');
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (10, 11, 'T010', '人工智能学院', '教授', 'A110');
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (11, 12, 'T011', '人工智能学院', '教授', 'A111');
INSERT INTO `teachers` (`id`, `user_id`, `teacher_id`, `department`, `title`, `office`) VALUES (19, 72, '123456', NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('ADMIN','TEACHER','STUDENT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=194 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of users
-- ----------------------------
BEGIN;
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (1, 'admin', '$2a$10$U8/cTJ9hncZfOJLcBXnfbOwuB4hNHdDRXBLKTMUYutqGQsoeX2KJy', 'ADMIN', 'admin@edu.com', '13800138000', '系统管理员', 'ACTIVE', '2025-11-02 11:12:17', '2025-12-16 16:05:31');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (2, 't001', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'wang@edu.com', '13800138001', '王教授', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (3, 't002', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'li@edu.com', '13800138002', '李副教授', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (4, 't003', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'zhang@edu.com', '13800138003', '张讲师', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (5, 't004', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'liu@edu.com', '13800138004', '刘教授', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (6, 't005', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'chen@edu.com', '13800138005', '陈副教授', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (7, 't006', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'yang@edu.com', '13800138006', '杨讲师', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (8, 't007', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'zhao@edu.com', '13800138007', '赵教授', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (9, 't008', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'qian@edu.com', '13800138008', '钱副教授', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (10, 't009', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'sun@edu.com', '13800138009', '孙讲师', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (11, 't010', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'zhou@edu.com', '13800138010', '周教授', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (12, 's001', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's001@stu.edu.com', '13800138100', '张一', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (13, 's002', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's002@stu.edu.com', '13800138101', '王二', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (14, 's003', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's003@stu.edu.com', '13800138102', '李三', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (15, 's004', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's004@stu.edu.com', '13800138103', '刘四', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (16, 's005', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's005@stu.edu.com', '13800138104', '陈五', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (17, 's006', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's006@stu.edu.com', '13800138105', '杨六', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (18, 's007', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's007@stu.edu.com', '13800138106', '赵七', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (19, 's008', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's008@stu.edu.com', '13800138107', '钱八', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (20, 's009', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's009@stu.edu.com', '13800138108', '孙九', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (21, 's010', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's010@stu.edu.com', '13800138109', '周十', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (22, 's011', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's011@stu.edu.com', '13800138110', '吴十一', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (23, 's012', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's012@stu.edu.com', '13800138111', '郑十二', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (24, 's013', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's013@stu.edu.com', '13800138112', '冯十三', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (25, 's014', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's014@stu.edu.com', '13800138113', '褚十四', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (26, 's015', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's015@stu.edu.com', '13800138114', '卫十五', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (27, 's016', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's016@stu.edu.com', '13800138115', '蒋十六', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (28, 's017', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's017@stu.edu.com', '13800138116', '沈十七', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (29, 's018', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's018@stu.edu.com', '13800138117', '韩十八', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (30, 's019', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's019@stu.edu.com', '13800138118', '朱十九', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (31, 's020', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's020@stu.edu.com', '13800138119', '秦二十', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (32, 's021', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's021@stu.edu.com', '13800138120', '尤二十一', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (33, 's022', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's022@stu.edu.com', '13800138121', '许二十二', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (34, 's023', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's023@stu.edu.com', '13800138122', '何二十三', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (35, 's024', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's024@stu.edu.com', '13800138123', '吕二十四', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (36, 's025', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's025@stu.edu.com', '13800138124', '施二十五', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (37, 's026', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's026@stu.edu.com', '13800138125', '孔二十六', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (38, 's027', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's027@stu.edu.com', '13800138126', '曹二十七', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (39, 's028', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's028@stu.edu.com', '13800138127', '严二十八', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (40, 's029', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's029@stu.edu.com', '13800138128', '华二十九', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (41, 's030', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's030@stu.edu.com', '13800138129', '金三十', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (42, 's031', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's031@stu.edu.com', '13800138130', '魏三十一', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (43, 's032', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's032@stu.edu.com', '13800138131', '陶三十二', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (44, 's033', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's033@stu.edu.com', '13800138132', '姜三十三', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (45, 's034', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's034@stu.edu.com', '13800138133', '戚三十四', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (46, 's035', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's035@stu.edu.com', '13800138134', '谢三十五', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (47, 's036', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's036@stu.edu.com', '13800138135', '邹三十六', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (48, 's037', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's037@stu.edu.com', '13800138136', '喻三十七', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (49, 's038', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's038@stu.edu.com', '13800138137', '柏三十八', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (50, 's039', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's039@stu.edu.com', '13800138138', '水三十九', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (51, 's040', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's040@stu.edu.com', '13800138139', '窦四十', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (52, 's041', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's041@stu.edu.com', '13800138140', '章四十一', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (53, 's042', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's042@stu.edu.com', '13800138141', '云四十二', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (54, 's043', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's043@stu.edu.com', '13800138142', '苏四十三', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (55, 's044', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's044@stu.edu.com', '13800138143', '潘四十四', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (56, 's045', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's045@stu.edu.com', '13800138144', '葛四十五', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (57, 's046', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's046@stu.edu.com', '13800138145', '奚四十六', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (58, 's047', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's047@stu.edu.com', '13800138146', '范四十七', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (59, 's048', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's048@stu.edu.com', '13800138147', '彭四十八', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (61, 's050', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's050@stu.edu.com', '13800138149', '鲁五十', 'ACTIVE', '2025-11-02 11:12:17', '2025-11-02 11:12:17');
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (72, 't123456', '$2a$10$5wN.LLB08STBhq4b/snXO.XtDbhcAn.enMDoWcHEVs231y.DqAyhi', 'TEACHER', '', '', '张屹', 'ACTIVE', NULL, NULL);
INSERT INTO `users` (`id`, `username`, `password`, `role`, `email`, `phone`, `real_name`, `status`, `created_at`, `updated_at`) VALUES (193, 's123456', '$2a$10$O1iTAdHK7zlpaXsxMoJ0h.RuVDQomXHtamaBRKlrW/VbaLtwOoHWK', 'STUDENT', '', '', '张志伟', 'ACTIVE', NULL, NULL);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
