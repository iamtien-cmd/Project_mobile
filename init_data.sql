USE handmade;

-- 1. Categories (5 bản ghi)
INSERT INTO category (category_id, category_name) VALUES
  (101, 'Trang sức'),
  (102, 'Trang trí nhà cửa'),
  (103, 'Giấy & Thiệp'),
  (104, 'Đèn & Nến'),
  (105, 'Túi & Ví');


-- 2. Products (50 bản ghi)
INSERT INTO product (product_id, name, description, image, price, category_id, favorite_id) VALUES
(1001, 'Vòng tay đá tự nhiên',      'Vòng tay đá thạch anh hồng handmade',   'vt-da-hong.jpg',    150000, 101, NULL),
(1002, 'Vòng tay gỗ khắc chữ',      'Vòng tay gỗ khắc tên theo yêu cầu',     'vt-go-khac.jpg',    120000, 101, NULL),
(1003, 'Dây chuyền ngọc trai',      'Dây chuyền ngọc trai biển xanh',        'dc-ngoc-trai.jpg',  250000, 101, NULL),
(1004, 'Khung ảnh gỗ',              'Khung ảnh gỗ tái chế handmade',         'khung-anh.jpg',     130000, 102, NULL),
(1005, 'Tranh thêu tay',            'Tranh thêu tay chủ đề thiên nhiên',     'tranh-theu.jpg',    300000, 102, NULL),
(1006, 'Thiệp pop-up',              'Thiệp pop-up 3D handmade mừng sinh nhật','thiep-popup.jpg',   50000, 103, NULL),
(1007, 'Sáp thơm cắm que',          'Sáp thơm cắm que mùi oải hương',        'sap-thom.jpg',       80000, 104, NULL),
(1008, 'Nến thơm thủ công',         'Nến thơm handmade hình hoa hồng',       'nen-thom.jpg',       90000, 104, NULL),
(1009, 'Ví da handmade',            'Ví da bò khâu tay nhỏ gọn',             'vi-da.jpg',         200000, 105, NULL),
(1010, 'Túi vải thổ cẩm',           'Túi vải thổ cẩm đeo chéo',               'tui-tho-cam.jpg',   180000, 105, NULL),
(1011, 'Khung ảnh gỗ nhỏ',          'Khung ảnh gỗ để bàn',                   'khung-ban.jpg',     100000, 102, NULL),
(1012, 'Móc khóa resin',            'Móc khóa resin hình hoa cúc',           'moc-khoa.jpg',       60000, 103, NULL),
(1013, 'Đèn ngủ đất sét',           'Đèn ngủ đất sét vẽ tay',                'den-ngu.jpg',       220000, 104, NULL),
(1014, 'Ốp lưng điện thoại',        'Ốp lưng gỗ khắc laser',                 'op-lung.jpg',       170000, 105, NULL),
(1015, 'Sổ tay bìa da',             'Sổ tay bìa da handmade A5',             'so-tay.jpg',        140000, 103, NULL),
(1016, 'Lót cốc thêu tay',          'Lót cốc thêu tay họa tiết hoa',         'lot-coc.jpg',        40000, 103, NULL),
(1017, 'Túi canvas vẽ tay',         'Túi canvas vẽ tay nghệ thuật',          'tui-canvas.jpg',    160000, 105, NULL),
(1018, 'Vòng cổ makrame',           'Vòng cổ makrame tông xanh',             'vong-co.jpg',       120000, 101, NULL),
(1019, 'Bình gốm sứ',               'Bình gốm sứ vẽ tay',                    'binh-gom.jpg',      260000, 102, NULL),
(1020, 'Thảm chùi chân',            'Thảm chùi chân len đan tay',            'tham-chan.jpg',     110000, 105, NULL),
(1021, 'Gối tựa lưng',              'Gối tựa lưng thêu tay',                 'goi-lung.jpg',      150000, 103, NULL),
(1022, 'Mặt dây chuyền gỗ',         'Dây chuyền gỗ khắc hình',               'dc-go.jpg',         130000, 101, NULL),
(1023, 'Khăn choàng len',           'Khăn choàng len dệt thủ công',          'khan-len.jpg',      190000, 105, NULL),
(1024, 'Ví cầm tay vải',            'Ví cầm tay vải hoa văn',                'vi-vai.jpg',         90000, 105, NULL),
(1025, 'Khung gương vintage',       'Khung gương gỗ vintage',                'guong.jpg',         240000, 102, NULL),
(1026, 'Đồng hồ gỗ',                'Đồng hồ gỗ treo tường handmade',        'dong-ho.jpg',       320000, 102, NULL),
(1027, 'Thiệp cưới',                'Thiệp cưới handmade sang trọng',        'thiep-cuoi.jpg',     70000, 103, NULL),
(1028, 'Bình hoa đất nung',         'Bình hoa đất nung mộc mạc',             'binh-dat.jpg',      210000, 102, NULL),
(1029, 'Tượng đất sét',             'Tượng đất sét mini',                    'tuong.jpg',          95000, 104, NULL),
(1030, 'Túi rút dây',               'Túi rút dây vải thô',                   'tui-rut.jpg',        80000, 105, NULL),
(1031, 'Vòng tay charm',            'Vòng tay charm mix hạt gỗ',             'vt-charm.jpg',      110000, 101, NULL),
(1032, 'Móc khóa vải',              'Móc khóa vải thêu tay',                 'moc-vai.jpg',        50000, 103, NULL),
(1033, 'Đèn lồng giấy',             'Đèn lồng giấy handmade',                'den-long.jpg',      120000, 104, NULL),
(1034, 'Tranh canvas',              'Tranh canvas in tay',                   'tranh-canvas.jpg',  280000, 102, NULL),
(1035, 'Bình giữ nhiệt',            'Bình giữ nhiệt vẽ tay',                 'binh-giu-nhiet.jpg',230000, 104, NULL),
(1036, 'Khung ảnh macrame',         'Khung ảnh macrame treo tường',          'macrame.jpg',       220000, 102, NULL),
(1037, 'Túi tote vẽ tay',           'Túi tote canvas vẽ tay',                'tui-tote.jpg',      150000, 105, NULL),
(1038, 'Lịch gỗ treo tường',        'Lịch gỗ handmade',                      'lich-go.jpg',       200000, 102, NULL),
(1039, 'Tượng gỗ mini',             'Tượng gỗ điêu khắc nhỏ',                'tuong-go.jpg',      180000, 102, NULL),
(1040, 'Vòng tay len',              'Vòng tay len móc tay',                  'vt-len.jpg',         90000, 101, NULL),
(1041, 'Mặt nạ giấy handmade',      'Mặt nạ giấy hoa văn',                   'mat-na.jpg',         60000, 104, NULL),
(1042, 'Sách tay vẽ',               'Sách tay vẽ A5',                        'sach-ve.jpg',        80000, 103, NULL),
(1043, 'Băng đô vải',               'Băng đô vải thắt nơ',                   'bang-do.jpg',        50000, 105, NULL),
(1044, 'Bông tai resin',            'Bông tai resin hình giọt nước',         'bong-tai.jpg',       70000, 103, NULL),
(1045, 'Bông tai thổ cẩm',          'Bông tai thổ cẩm nhiều màu',            'bong-tai2.jpg',      75000, 103, NULL),
(1046, 'Ví da nhỏ',                 'Ví da bò mini',                         'vi-da-mini.jpg',    160000, 105, NULL),
(1047, 'Khung ảnh kim loại',        'Khung ảnh kim loại mỏng',               'khung-kim.jpg',     210000, 102, NULL),
(1048, 'Thiệp Giáng Sinh',         'Thiệp Giáng Sinh 3D',                   'thiep-giang-sinh.jpg',60000,103, NULL),
(1049, 'Túi vải tái chế',           'Túi vải tái chế handmade',              'tui-tai-che.jpg',   140000, 105, NULL),
(1050, 'Móc khóa pha lê',           'Móc khóa pha lê handmade',              'moc-pha-le.jpg',     80000, 103, NULL);


INSERT INTO user (
  user_id, active, email, full_name, otp_code, otp_expiration, password, type
) VALUES 
(1, 1, 'minhhoa@example.com', 'Nguyễn Minh Hòa', '123456', '2025-04-10 23:59:59', '123456', 'New'),
(2, 1, 'hoamai@example.com', 'Trần Hoa Mai', '123456', '2025-04-10 23:59:59', '123456', 'New'),
(3, 1, 'thienan@example.com', 'Lê Thiên Ân', '123456', '2025-04-10 23:59:59', '123456', 'New'),
(4, 1, 'tuyettrinh@example.com', 'Phạm Tuyết Trinh', '123456', '2025-04-10 23:59:59', '123456', 'New'),
(5, 1, 'adminshop@example.com', 'Admin Handmade', '123456', '2025-04-10 23:59:59', '123456', 'New');
