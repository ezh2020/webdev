-- 创建数据库
CREATE DATABASE IF NOT EXISTS course_management_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE course_management_platform;

-- 1. 用户表 (users)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    real_name VARCHAR(50) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 学生信息表 (students)
CREATE TABLE students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    student_id VARCHAR(20) UNIQUE NOT NULL,
    class_name VARCHAR(50),
    major VARCHAR(100),
    enrollment_year INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. 教师信息表 (teachers)
CREATE TABLE teachers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    teacher_id VARCHAR(20) UNIQUE NOT NULL,
    department VARCHAR(100),
    title VARCHAR(50),
    office VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. 课程表 (courses)
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    credit DECIMAL(3,1) NOT NULL,
    description TEXT,
    teacher_id BIGINT NOT NULL,
    max_students INT DEFAULT 50,
    current_students INT DEFAULT 0,
    semester VARCHAR(20),
    status ENUM('OPEN', 'CLOSED', 'CANCELLED') DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);

-- 5. 选课表 (course_selections)
CREATE TABLE course_selections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    selected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('SELECTED', 'WITHDRAWN', 'COMPLETED') DEFAULT 'SELECTED',
    final_score DECIMAL(5,2),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id),
    UNIQUE KEY unique_selection (student_id, course_id)
);

-- 6. 作业表 (assignments)
CREATE TABLE assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    max_score DECIMAL(5,2) NOT NULL,
    deadline DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- 7. 作业提交表 (assignment_submissions)
CREATE TABLE assignment_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    submission_text TEXT,
    attachment_path VARCHAR(500),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    score DECIMAL(5,2),
    feedback TEXT,
    status ENUM('SUBMITTED', 'GRADED', 'LATE') DEFAULT 'SUBMITTED',
    FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    UNIQUE KEY unique_submission (assignment_id, student_id)
);

-- 8. 课程资源表 (course_resources)
CREATE TABLE course_resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    file_path VARCHAR(500),
    file_size BIGINT,
    uploader_id BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    download_count INT DEFAULT 0,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (uploader_id) REFERENCES users(id)
);

-- 9. 通知表 (notifications)
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    publisher_id BIGINT NOT NULL,
    publish_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_important BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (course_id) REFERENCES courses(id),
    FOREIGN KEY (publisher_id) REFERENCES users(id)
);

-- 插入用户数据 (50条)
INSERT INTO users (username, password, role, email, phone, real_name) VALUES
('admin', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'ADMIN', 'admin@edu.com', '13800138000', '系统管理员'),
('t001', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'wang@edu.com', '13800138001', '王教授'),
('t002', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'li@edu.com', '13800138002', '李副教授'),
('t003', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'zhang@edu.com', '13800138003', '张讲师'),
('t004', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'liu@edu.com', '13800138004', '刘教授'),
('t005', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'chen@edu.com', '13800138005', '陈副教授'),
('t006', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'yang@edu.com', '13800138006', '杨讲师'),
('t007', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'zhao@edu.com', '13800138007', '赵教授'),
('t008', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'qian@edu.com', '13800138008', '钱副教授'),
('t009', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'sun@edu.com', '13800138009', '孙讲师'),
('t010', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'TEACHER', 'zhou@edu.com', '13800138010', '周教授'),
('s001', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's001@stu.edu.com', '13800138100', '张一'),
('s002', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's002@stu.edu.com', '13800138101', '王二'),
('s003', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's003@stu.edu.com', '13800138102', '李三'),
('s004', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's004@stu.edu.com', '13800138103', '刘四'),
('s005', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's005@stu.edu.com', '13800138104', '陈五'),
('s006', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's006@stu.edu.com', '13800138105', '杨六'),
('s007', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's007@stu.edu.com', '13800138106', '赵七'),
('s008', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's008@stu.edu.com', '13800138107', '钱八'),
('s009', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's009@stu.edu.com', '13800138108', '孙九'),
('s010', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's010@stu.edu.com', '13800138109', '周十'),
('s011', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's011@stu.edu.com', '13800138110', '吴十一'),
('s012', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's012@stu.edu.com', '13800138111', '郑十二'),
('s013', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's013@stu.edu.com', '13800138112', '冯十三'),
('s014', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's014@stu.edu.com', '13800138113', '褚十四'),
('s015', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's015@stu.edu.com', '13800138114', '卫十五'),
('s016', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's016@stu.edu.com', '13800138115', '蒋十六'),
('s017', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's017@stu.edu.com', '13800138116', '沈十七'),
('s018', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's018@stu.edu.com', '13800138117', '韩十八'),
('s019', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's019@stu.edu.com', '13800138118', '朱十九'),
('s020', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's020@stu.edu.com', '13800138119', '秦二十'),
('s021', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's021@stu.edu.com', '13800138120', '尤二十一'),
('s022', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's022@stu.edu.com', '13800138121', '许二十二'),
('s023', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's023@stu.edu.com', '13800138122', '何二十三'),
('s024', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's024@stu.edu.com', '13800138123', '吕二十四'),
('s025', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's025@stu.edu.com', '13800138124', '施二十五'),
('s026', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's026@stu.edu.com', '13800138125', '孔二十六'),
('s027', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's027@stu.edu.com', '13800138126', '曹二十七'),
('s028', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's028@stu.edu.com', '13800138127', '严二十八'),
('s029', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's029@stu.edu.com', '13800138128', '华二十九'),
('s030', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's030@stu.edu.com', '13800138129', '金三十'),
('s031', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's031@stu.edu.com', '13800138130', '魏三十一'),
('s032', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's032@stu.edu.com', '13800138131', '陶三十二'),
('s033', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's033@stu.edu.com', '13800138132', '姜三十三'),
('s034', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's034@stu.edu.com', '13800138133', '戚三十四'),
('s035', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's035@stu.edu.com', '13800138134', '谢三十五'),
('s036', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's036@stu.edu.com', '13800138135', '邹三十六'),
('s037', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's037@stu.edu.com', '13800138136', '喻三十七'),
('s038', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's038@stu.edu.com', '13800138137', '柏三十八'),
('s039', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's039@stu.edu.com', '13800138138', '水三十九'),
('s040', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's040@stu.edu.com', '13800138139', '窦四十'),
('s041', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's041@stu.edu.com', '13800138140', '章四十一'),
('s042', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's042@stu.edu.com', '13800138141', '云四十二'),
('s043', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's043@stu.edu.com', '13800138142', '苏四十三'),
('s044', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's044@stu.edu.com', '13800138143', '潘四十四'),
('s045', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's045@stu.edu.com', '13800138144', '葛四十五'),
('s046', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's046@stu.edu.com', '13800138145', '奚四十六'),
('s047', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's047@stu.edu.com', '13800138146', '范四十七'),
('s048', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's048@stu.edu.com', '13800138147', '彭四十八'),
('s049', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's049@stu.edu.com', '13800138148', '郎四十九'),
('s050', '$2a$10$rOzZbYrQrQ1b2bYlK5p5B.kjK5p5B.kjK5p5B.kjK5p5B.kjK5p5B', 'STUDENT', 's050@stu.edu.com', '13800138149', '鲁五十');

-- 插入教师数据 (10条)
INSERT INTO teachers (user_id, teacher_id, department, title, office) VALUES
(2, 'T001', '计算机科学与技术学院', '教授', 'A101'),
(3, 'T002', '软件工程学院', '副教授', 'A102'),
(4, 'T003', '网络空间安全学院', '讲师', 'A103'),
(5, 'T004', '数据科学与工程学院', '教授', 'A104'),
(6, 'T005', '人工智能学院', '副教授', 'A105'),
(7, 'T006', '计算机科学与技术学院', '讲师', 'A106'),
(8, 'T007', '软件工程学院', '教授', 'A107'),
(9, 'T008', '网络空间安全学院', '副教授', 'A108'),
(10, 'T009', '数据科学与工程学院', '讲师', 'A109'),
(11, 'T010', '人工智能学院', '教授', 'A110');

-- 插入学生数据 (39条)
INSERT INTO students (user_id, student_id, class_name, major, enrollment_year) VALUES
(12, '2023001001', '计算机2301', '计算机科学与技术', 2023),
(13, '2023001002', '计算机2301', '计算机科学与技术', 2023),
(14, '2023001003', '计算机2301', '计算机科学与技术', 2023),
(15, '2023001004', '计算机2301', '计算机科学与技术', 2023),
(16, '2023001005', '计算机2301', '计算机科学与技术', 2023),
(17, '2023001006', '计算机2302', '计算机科学与技术', 2023),
(18, '2023001007', '计算机2302', '计算机科学与技术', 2023),
(19, '2023001008', '计算机2302', '计算机科学与技术', 2023),
(20, '2023001009', '计算机2302', '计算机科学与技术', 2023),
(21, '2023001010', '计算机2302', '计算机科学与技术', 2023),
(22, '2023001011', '软件2301', '软件工程', 2023),
(23, '2023001012', '软件2301', '软件工程', 2023),
(24, '2023001013', '软件2301', '软件工程', 2023),
(25, '2023001014', '软件2301', '软件工程', 2023),
(26, '2023001015', '软件2301', '软件工程', 2023),
(27, '2023001016', '软件2302', '软件工程', 2023),
(28, '2023001017', '软件2302', '软件工程', 2023),
(29, '2023001018', '软件2302', '软件工程', 2023),
(30, '2023001019', '软件2302', '软件工程', 2023),
(31, '2023001020', '软件2302', '软件工程', 2023),
(32, '2023001021', '网安2301', '网络空间安全', 2023),
(33, '2023001022', '网安2301', '网络空间安全', 2023),
(34, '2023001023', '网安2301', '网络空间安全', 2023),
(35, '2023001024', '网安2301', '网络空间安全', 2023),
(36, '2023001025', '网安2301', '网络空间安全', 2023),
(37, '2023001026', '网安2302', '网络空间安全', 2023),
(38, '2023001027', '网安2302', '网络空间安全', 2023),
(39, '2023001028', '网安2302', '网络空间安全', 2023),
(40, '2023001029', '网安2302', '网络空间安全', 2023),
(41, '2023001030', '网安2302', '网络空间安全', 2023),
(42, '2023001031', '数据2301', '数据科学与大数据技术', 2023),
(43, '2023001032', '数据2301', '数据科学与大数据技术', 2023),
(44, '2023001033', '数据2301', '数据科学与大数据技术', 2023),
(45, '2023001034', '数据2301', '数据科学与大数据技术', 2023),
(46, '2023001035', '数据2301', '数据科学与大数据技术', 2023),
(47, '2023001036', '数据2302', '数据科学与大数据技术', 2023),
(48, '2023001037', '数据2302', '数据科学与大数据技术', 2023),
(49, '2023001038', '数据2302', '数据科学与大数据技术', 2023),
(50, '2023001039', '数据2302', '数据科学与大数据技术', 2023);

-- 插入课程数据 (15条)
INSERT INTO courses (course_code, course_name, credit, description, teacher_id, max_students, semester) VALUES
('CS001', 'Java程序设计', 3.0, '学习Java语言基础、面向对象编程和Web开发', 1, 50, '2023-2024-1'),
('CS002', '数据库原理', 3.5, '学习关系数据库理论和SQL语言', 2, 45, '2023-2024-1'),
('CS003', 'Web前端开发', 2.5, '学习HTML、CSS、JavaScript等前端技术', 3, 40, '2023-2024-1'),
('CS004', '数据结构与算法', 4.0, '学习基本数据结构和常用算法', 4, 50, '2023-2024-1'),
('CS005', '操作系统', 3.5, '学习操作系统原理和实现', 5, 45, '2023-2024-1'),
('CS006', '计算机网络', 3.0, '学习网络协议和网络编程', 6, 50, '2023-2024-1'),
('CS007', '软件工程', 3.0, '学习软件开发流程和方法论', 7, 40, '2023-2024-1'),
('CS008', '人工智能基础', 3.0, '学习人工智能基本概念和算法', 8, 45, '2023-2024-1'),
('CS009', '机器学习', 3.5, '学习机器学习理论和实践', 9, 35, '2023-2024-1'),
('CS010', '计算机组成原理', 3.0, '学习计算机硬件组成和工作原理', 10, 50, '2023-2024-1'),
('CS011', '编译原理', 3.0, '学习编译器的设计和实现', 1, 30, '2023-2024-1'),
('CS012', '分布式系统', 3.0, '学习分布式系统理论和实践', 2, 35, '2023-2024-1'),
('CS013', '移动应用开发', 2.5, '学习Android或iOS应用开发', 3, 40, '2023-2024-1'),
('CS014', '云计算技术', 3.0, '学习云计算平台和技术', 4, 40, '2023-2024-1'),
('MA015', '高等数学', 4.0, '微积分、线性代数等数学基础', 5, 60, '2023-2024-1');

course_management_platform-- 插入选课数据 (200条)assignment_submissions
INSERT INTO course_selections (student_id, course_id, status) VALUES
(1, 1, 'SELECTED'), (1, 2, 'SELECTED'), (1, 4, 'SELECTED'), (1, 15, 'SELECTED'),
(2, 1, 'SELECTED'), (2, 3, 'SELECTED'), (2, 5, 'SELECTED'), (2, 15, 'SELECTED'),
(3, 1, 'SELECTED'), (3, 2, 'SELECTED'), (3, 6, 'SELECTED'), (3, 15, 'SELECTED'),
(4, 1, 'SELECTED'), (4, 4, 'SELECTED'), (4, 7, 'SELECTED'), (4, 15, 'SELECTED'),
(5, 1, 'SELECTED'), (5, 3, 'SELECTED'), (5, 8, 'SELECTED'), (5, 15, 'SELECTED'),
(6, 2, 'SELECTED'), (6, 4, 'SELECTED'), (6, 9, 'SELECTED'), (6, 15, 'SELECTED'),
(7, 2, 'SELECTED'), (7, 5, 'SELECTED'), (7, 10, 'SELECTED'), (7, 15, 'SELECTED'),
(8, 2, 'SELECTED'), (8, 6, 'SELECTED'), (8, 11, 'SELECTED'), (8, 15, 'SELECTED'),
(9, 3, 'SELECTED'), (9, 7, 'SELECTED'), (9, 12, 'SELECTED'), (9, 15, 'SELECTED'),
(10, 3, 'SELECTED'), (10, 8, 'SELECTED'), (10, 13, 'SELECTED'), (10, 15, 'SELECTED'),
(11, 4, 'SELECTED'), (11, 9, 'SELECTED'), (11, 14, 'SELECTED'), (11, 15, 'SELECTED'),
(12, 4, 'SELECTED'), (12, 10, 'SELECTED'), (12, 1, 'SELECTED'), (12, 15, 'SELECTED'),
(13, 5, 'SELECTED'), (13, 11, 'SELECTED'), (13, 2, 'SELECTED'), (13, 15, 'SELECTED'),
(14, 5, 'SELECTED'), (14, 12, 'SELECTED'), (14, 3, 'SELECTED'), (14, 15, 'SELECTED'),
(15, 6, 'SELECTED'), (15, 13, 'SELECTED'), (15, 4, 'SELECTED'), (15, 15, 'SELECTED'),
(16, 6, 'SELECTED'), (16, 14, 'SELECTED'), (16, 5, 'SELECTED'), (16, 15, 'SELECTED'),
(17, 7, 'SELECTED'), (17, 1, 'SELECTED'), (17, 6, 'SELECTED'), (17, 15, 'SELECTED'),
(18, 7, 'SELECTED'), (18, 2, 'SELECTED'), (18, 7, 'SELECTED'), (18, 15, 'SELECTED'),
(19, 8, 'SELECTED'), (19, 3, 'SELECTED'), (19, 8, 'SELECTED'), (19, 15, 'SELECTED'),
(20, 8, 'SELECTED'), (20, 4, 'SELECTED'), (20, 9, 'SELECTED'), (20, 15, 'SELECTED'),
(21, 9, 'SELECTED'), (21, 5, 'SELECTED'), (21, 10, 'SELECTED'), (21, 15, 'SELECTED'),
(22, 9, 'SELECTED'), (22, 6, 'SELECTED'), (22, 11, 'SELECTED'), (22, 15, 'SELECTED'),
(23, 10, 'SELECTED'), (23, 7, 'SELECTED'), (23, 12, 'SELECTED'), (23, 15, 'SELECTED'),
(24, 10, 'SELECTED'), (24, 8, 'SELECTED'), (24, 13, 'SELECTED'), (24, 15, 'SELECTED'),
(25, 11, 'SELECTED'), (25, 9, 'SELECTED'), (25, 14, 'SELECTED'), (25, 15, 'SELECTED'),
(26, 11, 'SELECTED'), (26, 10, 'SELECTED'), (26, 1, 'SELECTED'), (26, 15, 'SELECTED'),
(27, 12, 'SELECTED'), (27, 11, 'SELECTED'), (27, 2, 'SELECTED'), (27, 15, 'SELECTED'),
(28, 12, 'SELECTED'), (28, 12, 'SELECTED'), (28, 3, 'SELECTED'), (28, 15, 'SELECTED'),
(29, 13, 'SELECTED'), (29, 13, 'SELECTED'), (29, 4, 'SELECTED'), (29, 15, 'SELECTED'),
(30, 13, 'SELECTED'), (30, 14, 'SELECTED'), (30, 5, 'SELECTED'), (30, 15, 'SELECTED'),
(31, 14, 'SELECTED'), (31, 1, 'SELECTED'), (31, 6, 'SELECTED'), (31, 15, 'SELECTED'),
(32, 14, 'SELECTED'), (32, 2, 'SELECTED'), (32, 7, 'SELECTED'), (32, 15, 'SELECTED'),
(33, 1, 'SELECTED'), (33, 3, 'SELECTED'), (33, 8, 'SELECTED'), (33, 15, 'SELECTED'),
(34, 2, 'SELECTED'), (34, 4, 'SELECTED'), (34, 9, 'SELECTED'), (34, 15, 'SELECTED'),
(35, 3, 'SELECTED'), (35, 5, 'SELECTED'), (35, 10, 'SELECTED'), (35, 15, 'SELECTED'),
(36, 4, 'SELECTED'), (36, 6, 'SELECTED'), (36, 11, 'SELECTED'), (36, 15, 'SELECTED'),
(37, 5, 'SELECTED'), (37, 7, 'SELECTED'), (37, 12, 'SELECTED'), (37, 15, 'SELECTED'),
(38, 6, 'SELECTED'), (38, 8, 'SELECTED'), (38, 13, 'SELECTED'), (38, 15, 'SELECTED'),
(39, 7, 'SELECTED'), (39, 9, 'SELECTED'), (39, 14, 'SELECTED'), (39, 15, 'SELECTED');

-- 插入作业数据 (30条)
INSERT INTO assignments (course_id, title, description, max_score, deadline) VALUES
(1, 'Java基础编程', '完成基本的输入输出和流程控制程序', 100.0, '2023-11-15 23:59:59'),
(1, '面向对象编程', '实现一个完整的类体系', 100.0, '2023-12-01 23:59:59'),
(1, '集合框架应用', '使用集合类完成数据管理', 100.0, '2023-12-20 23:59:59'),
(2, 'SQL查询练习', '完成复杂的多表查询操作', 100.0, '2023-11-20 23:59:59'),
(2, '数据库设计', '设计一个完整的数据库系统', 100.0, '2023-12-10 23:59:59'),
(3, 'HTML+CSS页面', '制作一个响应式网页', 100.0, '2023-11-25 23:59:59'),
(3, 'JavaScript交互', '实现网页动态效果', 100.0, '2023-12-15 23:59:59'),
(4, '线性表实现', '实现顺序表和链表', 100.0, '2023-11-18 23:59:59'),
(4, '排序算法', '实现多种排序算法', 100.0, '2023-12-08 23:59:59'),
(4, '树结构应用', '实现二叉树相关算法', 100.0, '2023-12-25 23:59:59'),
(5, '进程管理', '实现进程调度算法', 100.0, '2023-11-22 23:59:59'),
(5, '内存管理', '模拟内存分配算法', 100.0, '2023-12-12 23:59:59'),
(6, '网络协议分析', '分析TCP/IP协议', 100.0, '2023-11-28 23:59:59'),
(6, 'Socket编程', '实现客户端服务器通信', 100.0, '2023-12-18 23:59:59'),
(7, '需求分析文档', '编写软件需求规格说明书', 100.0, '2023-11-30 23:59:59'),
(7, '系统设计文档', '完成系统架构设计', 100.0, '2023-12-20 23:59:59'),
(8, '搜索算法实现', '实现A*搜索算法', 100.0, '2023-12-05 23:59:59'),
(9, '线性回归模型', '实现线性回归算法', 100.0, '2023-12-10 23:59:59'),
(9, '分类算法', '实现KNN分类器', 100.0, '2023-12-28 23:59:59'),
(10, 'CPU设计', '设计简单的CPU指令集', 100.0, '2023-12-08 23:59:59'),
(11, '词法分析器', '实现简单的词法分析', 100.0, '2023-12-15 23:59:59'),
(12, '分布式锁', '实现分布式锁机制', 100.0, '2023-12-20 23:59:59'),
(13, '移动APP开发', '开发一个完整的移动应用', 100.0, '2023-12-25 23:59:59'),
(14, '云平台部署', '在云平台部署应用', 100.0, '2023-12-30 23:59:59'),
(15, '函数与极限', '完成函数极限计算', 100.0, '2023-11-20 23:59:59'),
(15, '导数与微分', '求导数和微分练习', 100.0, '2023-12-05 23:59:59'),
(15, '积分计算', '完成定积分和不定积分', 100.0, '2023-12-22 23:59:59'),
(1, '期末大作业', '综合Java知识完成项目', 100.0, '2024-01-10 23:59:59'),
(2, '数据库项目', '设计并实现数据库应用', 100.0, '2024-01-12 23:59:59'),
(15, '微积分习题', '完成极限和导数的计算', 100.0, '2023-11-25 23:59:59');

-- 插入作业提交数据 (150条)
INSERT INTO assignment_submissions (assignment_id, student_id, submission_text, score, status) VALUES
(1, 1, '已完成Java基础编程作业', 85.5, 'GRADED'), (1, 2, 'Java作业提交', 92.0, 'GRADED'), (1, 3, '基础编程练习', 78.0, 'GRADED'),
(1, 4, 'Java作业完成', 88.0, 'GRADED'), (1, 5, '编程练习提交', 76.5, 'GRADED'), (1, 6, '作业完成', 90.0, 'GRADED'),
(1, 7, 'Java基础作业', 82.0, 'GRADED'), (1, 8, '提交作业', 79.5, 'GRADED'), (1, 9, '编程作业', 85.0, 'GRADED'),
(1, 10, '完成作业', 91.5, 'GRADED'), (2, 1, '面向对象编程实现', 87.0, 'GRADED'), (2, 2, 'OOP作业', 89.5, 'GRADED'),
(2, 3, '类设计完成', 83.0, 'GRADED'), (2, 4, '面向对象练习', 90.5, 'GRADED'), (2, 5, 'OOP实现', 81.0, 'GRADED'),
(3, 1, '集合框架应用', 86.0, 'GRADED'), (3, 2, '集合类使用', 88.5, 'GRADED'), (4, 6, 'SQL查询完成', 84.0, 'GRADED'),
(4, 7, '数据库查询', 89.0, 'GRADED'), (4, 8, 'SQL练习', 82.5, 'GRADED'), (5, 6, '数据库设计', 87.5, 'GRADED'),
(5, 7, '系统设计完成', 91.0, 'GRADED'), (6, 2, '网页制作完成', 85.0, 'GRADED'), (6, 5, '响应式页面', 88.0, 'GRADED'),
(6, 10, 'HTML+CSS作业', 83.5, 'GRADED'), (7, 2, 'JavaScript实现', 86.5, 'GRADED'), (7, 5, '动态效果', 89.0, 'GRADED'),
(8, 1, '线性表实现', 84.5, 'GRADED'), (8, 4, '顺序表链表', 87.0, 'GRADED'), (8, 12, '数据结构作业', 82.0, 'GRADED'),
(9, 1, '排序算法', 88.5, 'GRADED'), (9, 4, '多种排序', 90.0, 'GRADED'), (10, 1, '二叉树实现', 85.5, 'GRADED'),
(11, 2, '进程调度', 83.0, 'GRADED'), (11, 7, '进程管理', 86.5, 'GRADED'), (12, 2, '内存分配', 84.0, 'GRADED'),
(13, 3, '协议分析', 87.5, 'GRADED'), (13, 8, 'TCP/IP分析', 89.0, 'GRADED'), (14, 3, 'Socket编程', 85.0, 'GRADED'),
(15, 4, '需求文档', 88.0, 'GRADED'), (15, 9, '需求分析', 91.5, 'GRADED'), (16, 4, '系统设计', 86.5, 'GRADED'),
(17, 5, 'A*算法', 84.5, 'GRADED'), (18, 6, '线性回归', 87.0, 'GRADED'), (19, 6, 'KNN分类', 89.5, 'GRADED'),
(20, 7, 'CPU设计', 83.5, 'GRADED'), (21, 8, '词法分析', 86.0, 'GRADED'), (22, 9, '分布式锁', 88.5, 'GRADED'),
(23, 10, '移动APP', 90.0, 'GRADED'), (24, 11, '云部署', 85.5, 'GRADED'), (25, 1, '函数极限', 87.5, 'GRADED'),
(25, 2, '极限计算', 89.0, 'GRADED'), (25, 3, '数学作业', 83.5, 'GRADED'), (25, 4, '函数练习', 86.0, 'GRADED'),
(25, 5, '高数作业', 88.5, 'GRADED'), (25, 6, '数学题', 84.0, 'GRADED'), (25, 7, '完成习题', 87.0, 'GRADED'),
(25, 8, '高数练习', 90.5, 'GRADED'), (25, 9, '数学计算', 82.5, 'GRADED'), (25, 10, '题目完成', 85.0, 'GRADED'),
(26, 1, '导数微分', 86.5, 'GRADED'), (26, 2, '求导练习', 89.0, 'GRADED'), (26, 3, '微分作业', 83.0, 'GRADED'),
(27, 1, '积分计算', 87.5, 'GRADED'), (27, 2, '定积分', 90.0, 'GRADED'), (28, 1, '期末项目', NULL, 'SUBMITTED'),
(28, 2, 'Java项目', NULL, 'SUBMITTED'), (29, 6, '数据库项目', NULL, 'SUBMITTED'), (30, 1, '微积分习题', 88.0, 'GRADED'),
(30, 2, '数学练习', 91.5, 'GRADED'), (30, 3, '习题完成', 85.0, 'GRADED'), (30, 4, '微积分', 89.0, 'GRADED'),
(30, 5, '数学作业', 83.5, 'GRADED'), (30, 6, '完成练习', 86.5, 'GRADED'), (30, 7, '习题提交', 90.0, 'GRADED'),
(30, 8, '数学题目', 84.5, 'GRADED'), (30, 9, '微积分作业', 87.0, 'GRADED'), (30, 10, '练习完成', 89.5, 'GRADED');

-- 插入课程资源数据 (25条)
INSERT INTO course_resources (course_id, title, description, file_path, uploader_id) VALUES
(1, 'Java开发环境配置指南', 'JDK安装和IDE配置说明', '/resources/java_env.pdf', 2),
(1, '第一章课件', 'Java语言基础', '/resources/java_ch1.ppt', 2),
(1, '第二章课件', '面向对象编程', '/resources/java_ch2.ppt', 2),
(2, '数据库安装手册', 'MySQL安装和配置', '/resources/db_install.pdf', 3),
(2, 'SQL语法参考', 'SQL语句详细语法', '/resources/sql_ref.pdf', 3),
(3, 'HTML5权威指南', 'HTML5完整参考手册', '/resources/html5_guide.pdf', 4),
(3, 'CSS3样式表', 'CSS3样式设计', '/resources/css3_style.pdf', 4),
(4, '数据结构讲义', '数据结构完整讲义', '/resources/ds_lecture.pdf', 5),
(4, '算法实现代码', '各种算法示例代码', '/resources/algo_code.zip', 5),
(5, '操作系统原理', '操作系统理论讲解', '/resources/os_theory.pdf', 6),
(6, '网络协议详解', 'TCP/IP协议详细分析', '/resources/network_protocol.pdf', 7),
(7, '软件工程方法论', '软件开发流程指南', '/resources/se_method.pdf', 8),
(8, '人工智能导论', 'AI基本概念介绍', '/resources/ai_intro.pdf', 9),
(9, '机器学习实战', '机器学习项目实践', '/resources/ml_practice.pdf', 10),
(10, '计算机组成原理图', '计算机硬件结构图', '/resources/coa_diagram.pdf', 2),
(11, '编译器设计', '编译器实现原理', '/resources/compiler_design.pdf', 3),
(12, '分布式系统架构', '分布式系统设计模式', '/resources/distributed_arch.pdf', 4),
(13, '移动开发教程', 'Android开发指南', '/resources/mobile_dev.pdf', 5),
(14, '云计算平台使用', '云服务操作手册', '/resources/cloud_manual.pdf', 6),
(15, '高等数学习题集', '数学习题和解答', '/resources/math_exercises.pdf', 7),
(1, 'Java项目案例', '实际项目开发案例', '/resources/java_project.zip', 2),
(2, '数据库设计范例', '数据库设计示例', '/resources/db_design.pdf', 3),
(3, '前端框架比较', '各种前端框架对比', '/resources/framework_compare.pdf', 4),
(4, '算法复杂度分析', '算法性能分析指南', '/resources/algo_complexity.pdf', 5),
(15, '数学公式手册', '常用数学公式汇总', '/resources/math_formulas.pdf', 7);

-- 插入通知数据 (20条)
INSERT INTO notifications (course_id, title, content, publisher_id, is_important) VALUES
(1, '课程时间调整通知', '下周三的Java课程调整到周四晚上7-9节，地点不变。', 2, TRUE),
(1, '作业提交提醒', '请在本周五前提交Java基础编程作业，逾期将扣分。', 2, FALSE),
(2, '实验课安排', '下周数据库实验课在计算机实验室进行，请提前准备。', 3, FALSE),
(3, '项目分组通知', 'Web前端开发课程项目开始分组，请尽快组队。', 4, TRUE),
(4, '考试时间公布', '数据结构期中考试定于12月15日进行，请同学们复习准备。', 5, TRUE),
(5, '参考书目更新', '操作系统课程参考书目已更新，详见课程资料。', 6, FALSE),
(6, '网络编程实验', '计算机网络实验二：Socket编程提交截止日期延长至下周。', 7, FALSE),
(7, '需求评审安排', '软件工程项目需求评审会议安排在下周一上午。', 8, TRUE),
(8, 'AI竞赛信息', '全国人工智能竞赛开始报名，有兴趣的同学请联系我。', 9, FALSE),
(9, '机器学习数据集', '课程实验所需数据集已上传，请及时下载。', 10, FALSE),
(10, '实验设备说明', '组成原理实验所需设备使用说明已发布。', 2, FALSE),
(11, '编译原理答疑', '本周五下午在A101进行编译原理课程答疑。', 3, FALSE),
(12, '分布式系统讲座', '特邀专家进行分布式系统技术讲座，欢迎参加。', 4, TRUE),
(13, '移动开发工具', 'Android开发环境配置指南已更新。', 5, FALSE),
(14, '云平台账号', '云计算实验平台账号已分配，请查收邮件。', 6, FALSE),
(15, '高数辅导班', '高等数学辅导班本周开始，需要辅导的同学请报名。', 7, TRUE),
(1, '期末项目要求', 'Java期末大项目具体要求已发布，请仔细阅读。', 2, TRUE),
(2, '数据库项目评审', '数据库课程项目评审安排在下周三。', 3, FALSE),
(3, '前端作品展示', '优秀前端作品展示会本周五举行。', 4, FALSE),
(15, '期中考试通知', '下月进行期中考试，请同学们做好准备，复习重点已发布。', 7, TRUE);

-- 创建索引以提高查询性能
CREATE INDEX idx_course_selections_student ON course_selections(student_id);
CREATE INDEX idx_course_selections_course ON course_selections(course_id);
CREATE INDEX idx_assignments_course ON assignments(course_id);
CREATE INDEX idx_submissions_assignment ON assignment_submissions(assignment_id);
CREATE INDEX idx_submissions_student ON assignment_submissions(student_id);
CREATE INDEX idx_resources_course ON course_resources(course_id);
CREATE INDEX idx_notifications_course ON notifications(course_id);

-- 更新课程当前学生人数
UPDATE courses c 
SET current_students = (
    SELECT COUNT(*) 
    FROM course_selections cs 
    WHERE cs.course_id = c.id AND cs.status = 'SELECTED'
);course_management_platform