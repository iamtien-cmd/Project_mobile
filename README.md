# Project_mobile
# Handmade
Link: https://drive.google.com/file/d/1GVgKhK26V8Z96uTEEyROwZ1zyFLVAPKq/view?usp=sharing 
# class diagram
![image](https://github.com/user-attachments/assets/e99d69dd-24fe-4892-a412-b3400295457d)


# usecase diagram
![image](https://github.com/user-attachments/assets/905ca45d-ae93-489a-89dc-d4e0b28ee625)


# ERD
![image](https://github.com/user-attachments/assets/23a250bc-ab81-46d0-999e-accf25a1aabe)

# Test đăng nhập
![image](https://github.com/user-attachments/assets/99c16214-d104-4da1-bdcb-cbb57f264f37)
![image](https://github.com/user-attachments/assets/822779fc-59a3-434b-8a8e-06805b018e6a)
![image](https://github.com/user-attachments/assets/282ab031-d296-4ed8-87db-ae19ee512bdf)

# Test đăng kí 
![image](https://github.com/user-attachments/assets/9cef1dd0-d4e1-4846-9946-8f2e7f881f9c)
![image](https://github.com/user-attachments/assets/025fcd18-8623-4746-bb65-2861dffd2ab4)
![image](https://github.com/user-attachments/assets/290ae64f-367c-4c16-be3c-4e125dda940e)
![image](https://github.com/user-attachments/assets/92eab083-e063-4da4-ac6c-82984aae83b7)
![image](https://github.com/user-attachments/assets/025a2466-f041-41a2-8397-59fd52440084)
![image](https://github.com/user-attachments/assets/fe350fa6-c58d-4009-a51e-a56172614fc8)
![image](https://github.com/user-attachments/assets/3f11e19b-7d0a-4e23-96b9-6dda4316e668)
![image](https://github.com/user-attachments/assets/0507dbea-c800-4be6-b0ff-9e97e80e22b9)
![image](https://github.com/user-attachments/assets/24618fc0-e15b-4b53-90b3-6548b937f2f6)
![image](https://github.com/user-attachments/assets/dd31a379-63eb-44e1-8d57-776fd3060821)

# Test quên mật khẩu
http://localhost:8080/api/auth/forgot-password
![image](https://github.com/user-attachments/assets/618bb238-33d0-4cd5-9af2-a01ac0c6f8b6)
![image](https://github.com/user-attachments/assets/60a47651-6fe5-4606-831e-b2d9f09ec1e9)
http://localhost:8080/api/auth/verifyOtpForgotPassword
![image](https://github.com/user-attachments/assets/daedd8b8-35ca-4caa-9a5e-f87dc74dfe46)
http://localhost:8080/api/auth/reset-password
![image](https://github.com/user-attachments/assets/ae27f811-7a74-4bec-b8a8-2ad012909c03)
![image](https://github.com/user-attachments/assets/4a48f938-ecca-418e-bf63-fd29974c1c70)
![image](https://github.com/user-attachments/assets/97b7313e-1e5c-48a8-bdb0-d758be4f5990)
![image](https://github.com/user-attachments/assets/8cafe4de-d65e-40e8-ab4e-4663afc77104)
![image](https://github.com/user-attachments/assets/6e824ed6-812a-491a-9120-a2e50b66ae79)
![image](https://github.com/user-attachments/assets/ba9018e8-863c-43ee-943b-c2b134a7f0ce)
![image](https://github.com/user-attachments/assets/8b2391cc-2fa6-4a74-9b66-4a5915e6c80b)
![image](https://github.com/user-attachments/assets/bcca94f0-02b2-4b44-8289-bb49efebc822)


# Test xem trạng thái đơn hàng
http://localhost:8080/api/orders/status/1 
Với 1 là id của user
![image](https://github.com/user-attachments/assets/c17ce919-1424-4f5d-8c50-6d1369c47998)
![image](https://github.com/user-attachments/assets/7a4772dd-0247-4713-82de-6ced52ca2c5b)


# Test thêm bình luận
http://localhost:8080/api/comments
![image](https://github.com/user-attachments/assets/c228e7d9-3eaa-4802-8210-da8fd66ffe8a)
Xem tất cả bình luận của 1 product
http://localhost:8080/api/comments/product/1001 Với 1001 là productId
![image](https://github.com/user-attachments/assets/01a863d2-1be9-4b15-b3b5-21b52b4209c7)
![image](https://github.com/user-attachments/assets/44f8c270-c970-42c5-a0b4-ea3d0316964d)
Đăng bình luận
Hosting hình ảnh thành đường dẫn public để csdl có thể truy cập bằng địa chỉ ipv4 của máy tính
http://localhost:8080/api/v1/upload/image. Sau khi hosting ta có thể truy cập hình ảnh qua link đã host: http://192.168.1.7:8080/api/images/ff5e81f4-74fc-4d1f-a5f7-d1da5aff3034.jpg
![image](https://github.com/user-attachments/assets/66ebcca6-f060-4435-9560-edb992853e85)
![image](https://github.com/user-attachments/assets/2e0665c9-a2ab-4558-86f7-49017f864b99)
![image](https://github.com/user-attachments/assets/84fc4214-e527-4629-b506-e0c369aa9e1f)

# Test nhắn tin với chatbot
Nếu gõ những từ khóa đã được train nó sẽ trả lời
![image](https://github.com/user-attachments/assets/917c8c9a-8f58-4c31-b023-ed15c8fcceed)
Nếu gõ những từ khóa chưa được biết nó sẽ trả lời "xin lỗi..."
![image](https://github.com/user-attachments/assets/8af698cf-c85d-4457-9a98-986c35c5a6a8)
![image](https://github.com/user-attachments/assets/d6de670c-8c43-4c0c-8ba0-d9fd195c6fa5)
# Tạo trigger ràng buộc khi nhập vào số lượng sản phẩm ở orderline, cột orderline.price = product.price * quantity

CREATE TRIGGER trg_order_line_before_update
BEFORE UPDATE ON order_line
FOR EACH ROW
BEGIN
    DECLARE product_unit_price DOUBLE;

    -- Chỉ tính toán lại nếu quantity hoặc product_id thay đổi
    -- Hoặc nếu ai đó cố tình cập nhật cột price trực tiếp (trigger này sẽ ghi đè)
    IF NEW.quantity <> OLD.quantity OR NEW.product_id <> OLD.product_id THEN
        -- Lấy giá của sản phẩm mới (nếu product_id thay đổi) hoặc sản phẩm hiện tại
        SELECT price INTO product_unit_price
        FROM product
        WHERE product_id = NEW.product_id;

        -- Kiểm tra nếu không tìm thấy giá sản phẩm
        IF product_unit_price IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Không thể cập nhật giá: Giá sản phẩm không tồn tại hoặc NULL.';
        END IF;

        -- Tính toán và gán giá trị mới cho cột price
        -- NEW.price tham chiếu đến giá trị SẮP được cập nhật của cột 'price'
        SET NEW.price = product_unit_price * NEW.quantity;
    END IF;
    -- Nếu bạn muốn LUÔN LUÔN ghi đè giá trị price mỗi khi có update, kể cả khi chỉ cột price được update
    -- thì có thể bỏ điều kiện IF ở trên và thực hiện tính toán trực tiếp:
    /*
    SELECT price INTO product_unit_price
    FROM product
    WHERE product_id = NEW.product_id;

    IF product_unit_price IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Không thể cập nhật giá: Giá sản phẩm không tồn tại hoặc NULL.';
    END IF;
    SET NEW.price = product_unit_price * NEW.quantity;
    */
END //

DELIMITER ;
# Tạo Trigger, Produce để khi cột items_subtotal sẽ được tự động tính bằng tổng các orderline có order_id giống nhau

DELIMITER //

CREATE TRIGGER trg_ol_after_insert_upd_subtotal
AFTER INSERT ON order_line
FOR EACH ROW
BEGIN
    DECLARE v_new_items_subtotal DOUBLE;

    -- Tính lại tổng items_subtotal cho order_id của dòng mới được chèn
    SELECT COALESCE(SUM(price), 0)
    INTO v_new_items_subtotal
    FROM order_line
    WHERE order_id = NEW.order_id;

    -- Cập nhật items_subtotal trong bảng orders
    UPDATE orders
    SET items_subtotal = v_new_items_subtotal
    WHERE order_id = NEW.order_id;
END //

DELIMITER ; 
DELIMITER //

CREATE TRIGGER trg_ol_after_update_upd_subtotal
AFTER UPDATE ON order_line
FOR EACH ROW
BEGIN
    DECLARE v_updated_items_subtotal DOUBLE;

    -- Nếu giá (price) hoặc order_id của order_line thay đổi
    IF NEW.price <> OLD.price OR NEW.order_id <> OLD.order_id THEN

        -- Cập nhật items_subtotal cho order_id MỚI (hoặc order_id hiện tại nếu nó không đổi)
        SELECT COALESCE(SUM(price), 0)
        INTO v_updated_items_subtotal
        FROM order_line
        WHERE order_id = NEW.order_id;

        UPDATE orders
        SET items_subtotal = v_updated_items_subtotal
        WHERE order_id = NEW.order_id;

        -- Nếu order_id thực sự đã thay đổi (order_line được chuyển sang order khác),
        -- thì cũng cần cập nhật items_subtotal cho order_id CŨ
        IF NEW.order_id <> OLD.order_id THEN
            SELECT COALESCE(SUM(price), 0)
            INTO v_updated_items_subtotal -- Có thể dùng lại biến
            FROM order_line
            WHERE order_id = OLD.order_id;

            UPDATE orders
            SET items_subtotal = v_updated_items_subtotal
            WHERE order_id = OLD.order_id;
        END IF;
    END IF;
END //

DELIMITER ;
DELIMITER //

CREATE TRIGGER trg_ol_after_delete_upd_subtotal
AFTER DELETE ON order_line
FOR EACH ROW
BEGIN
    DECLARE v_remaining_items_subtotal DOUBLE;

    -- Tính lại tổng items_subtotal cho order_id của dòng đã bị xóa
    SELECT COALESCE(SUM(price), 0)
    INTO v_remaining_items_subtotal
    FROM order_line
    WHERE order_id = OLD.order_id; -- OLD.order_id là order_id của dòng vừa bị xóa

    -- Cập nhật items_subtotal trong bảng orders
    UPDATE orders
    SET items_subtotal = v_remaining_items_subtotal
    WHERE order_id = OLD.order_id;
END //

DELIMITER ;

# Trigger kiểm tra khi đơn hàng đã được thanh toán và được nhận xét thì tổng giá trị đơn hàng sẽ không thay đổi
DELIMITER //

CREATE TRIGGER trg_prevent_subtotal_change_after_received
BEFORE UPDATE ON orders
FOR EACH ROW
BEGIN
    -- Nếu trạng thái đã là RECEIVED hoặc REVIEWED thì không cho thay đổi subtotal và total
    IF OLD.status IN ('RECEIVED', 'REVIEWED') THEN
        SET NEW.items_subtotal = OLD.items_subtotal;
        SET NEW.total_price = OLD.total_price;
    END IF;
END;
//

DELIMITER ;
# Trigger kiểm tra khi trạng thái đơn hàng là RECEIVED và REVIEWED thì giá tiền Orderline sẽ không được thay đổi
DELIMITER //

CREATE TRIGGER trg_prevent_orderline_update_after_received
BEFORE UPDATE ON order_line
FOR EACH ROW
BEGIN
    DECLARE v_order_status VARCHAR(20);

    -- Lấy trạng thái của đơn hàng tương ứng
    SELECT status INTO v_order_status
    FROM orders
    WHERE order_id = OLD.order_id;

    -- Nếu trạng thái là RECEIVED hoặc REVIEWED thì chặn cập nhật
    IF v_order_status IN ('RECEIVED', 'REVIEWED') THEN
        -- Giữ nguyên giá và số lượng (chặn cập nhật)
        SET NEW.price = OLD.price;
        SET NEW.quantity = OLD.quantity;
    END IF;
END;
//

DELIMITER ;



#Frontend trang home
![image](https://github.com/user-attachments/assets/6f61a17a-5f87-4e18-974b-5cc441778b6b)















