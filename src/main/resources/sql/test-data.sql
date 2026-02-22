-- ================================================
-- 택배 쇼핑몰 테스트 데이터
-- ================================================

-- 1. courier_config (택배 서비스 설정)
INSERT INTO courier_config (id, enabled, island_surcharge, notice_text, sender_name, sender_phone, sender_phone2, sender_address, sender_detail_address, created_at, updated_at)
VALUES (1, true, 3000.00, '주문 후 2~3일 내 발송됩니다. 도서산간 지역은 추가 배송비가 발생할 수 있습니다.', '과일맛집', '010-1234-5678', '02-123-4567', '서울특별시 강남구 테헤란로 123', '1층 과일맛집', NOW(), NOW())
ON DUPLICATE KEY UPDATE id = id;

-- 2. courier_product_category (카테고리)
INSERT INTO courier_product_category (id, name, sort_order, created_at) VALUES
(1, '제철 과일', 0, NOW()),
(2, '열대 과일', 1, NOW()),
(3, '선물세트', 2, NOW()),
(4, '건과일/견과', 3, NOW());

-- 3. shipping_fee_templates (배송 정책)
INSERT INTO shipping_fee_templates (id, name, base_fee, per_quantity_fee, free_shipping_min_amount, active, sort_order, created_at, updated_at) VALUES
(1, '기본 배송', 3000.00, NULL, 50000.00, true, 0, NOW(), NOW()),
(2, '무료 배송', 0.00, NULL, NULL, true, 1, NOW(), NOW()),
(3, '냉장 배송', 5000.00, NULL, 80000.00, true, 2, NOW(), NOW());

-- 4. shipping_fee_policies (수량별 배송비)
INSERT INTO shipping_fee_policies (id, min_quantity, max_quantity, fee, sort_order, active, created_at, updated_at) VALUES
(1, 1, 2, 3000.00, 0, true, NOW(), NOW()),
(2, 3, 5, 5000.00, 1, true, NOW(), NOW()),
(3, 6, 10, 7000.00, 2, true, NOW(), NOW()),
(4, 11, 99, 10000.00, 3, true, NOW(), NOW());

-- 5. courier_products (상품 10개)
INSERT INTO courier_products (id, name, product_url, price, stock, visible, description, sort_order, recommended, recommend_order, total_sold, shipping_fee_template_id, created_at, updated_at) VALUES
(1, '성주 꿀참외 2kg (특)', 'products/chamoe-2kg.jpg', 18900.00, 50, true, '<p>달콤한 <strong>성주 꿀참외</strong>를 산지에서 직접 보내드립니다.</p><p>당도 선별 14Brix 이상, 특등급만 엄선했습니다.</p>', -10, true, 0, 127, 1, NOW(), NOW()),
(2, '나주 배 선물세트 5kg', 'products/naju-pear-5kg.jpg', 45000.00, 30, true, '<p>나주 신고배 <strong>선물세트</strong></p><ul><li>5kg (7~9과)</li><li>고급 포장</li></ul>', -9, true, 1, 89, 2, NOW(), NOW()),
(3, '제주 감귤 3kg', 'products/jeju-gamgul-3kg.jpg', 15000.00, 100, true, '<p>제주도 노지 감귤 3kg</p><p>새콤달콤한 겨울 간식!</p>', -8, false, 0, 215, 1, NOW(), NOW()),
(4, '애플망고 1kg (2~3과)', 'products/apple-mango-1kg.jpg', 32000.00, 20, true, '<p>국내산 <strong>애플망고</strong></p><p>진한 향과 부드러운 과육이 특징입니다.</p>', -7, true, 2, 43, 3, NOW(), NOW()),
(5, '샤인머스캣 2kg', 'products/shine-muscat-2kg.jpg', 28000.00, 40, true, '<p>당도 선별 <strong>샤인머스캣</strong> 2kg</p><p>아삭하고 달콤한 프리미엄 포도</p>', -6, true, 3, 178, 1, NOW(), NOW()),
(6, '혼합 견과 선물세트 1kg', 'products/mixed-nuts-1kg.jpg', 25000.00, 60, true, '<p>아몬드, 호두, 캐슈넛, 마카다미아 혼합</p><p>무염 로스팅</p>', -5, false, 0, 92, 2, NOW(), NOW()),
(7, '건망고 500g', 'products/dried-mango-500g.jpg', 12000.00, 80, true, '<p>필리핀산 건망고 500g</p><p>설탕 무첨가, 자연 건조</p>', -4, false, 0, 156, 1, NOW(), NOW()),
(8, '청송 사과 5kg (특)', 'products/cheongsong-apple-5kg.jpg', 35000.00, 25, true, '<p>청송 부사 사과 5kg</p><p>아삭하고 단맛이 뛰어난 명품 사과</p>', -3, true, 4, 67, 1, NOW(), NOW()),
(9, '프리미엄 과일 바구니', 'products/premium-fruit-basket.jpg', 89000.00, 10, true, '<p>사과, 배, 샤인머스캣, 감귤 등</p><p><strong>프리미엄 과일 바구니 선물세트</strong></p>', -2, true, 5, 31, 2, NOW(), NOW()),
(10, '제주 한라봉 3kg', 'products/hallabong-3kg.jpg', 29000.00, 0, true, '<p>제주 한라봉 3kg (9~12과)</p><p>큼직한 사이즈, 달콤한 과즙</p>', -1, false, 0, 198, 1, NOW(), NOW());

-- 6. courier_product_category_mapping (상품-카테고리 매핑)
INSERT INTO courier_product_category_mapping (courier_product_id, category_id, sort_order, created_at, updated_at) VALUES
(1, 1, 0, NOW(), NOW()),
(2, 1, 0, NOW(), NOW()),
(2, 3, 1, NOW(), NOW()),
(3, 1, 0, NOW(), NOW()),
(4, 2, 0, NOW(), NOW()),
(5, 1, 0, NOW(), NOW()),
(6, 4, 0, NOW(), NOW()),
(6, 3, 1, NOW(), NOW()),
(7, 4, 0, NOW(), NOW()),
(7, 2, 1, NOW(), NOW()),
(8, 1, 0, NOW(), NOW()),
(8, 3, 1, NOW(), NOW()),
(9, 3, 0, NOW(), NOW()),
(10, 1, 0, NOW(), NOW());

-- 7. courier_product_option_groups + courier_product_options

-- 참외: 사이즈 옵션
INSERT INTO courier_product_option_groups (id, courier_product_id, name, required, sort_order, created_at, updated_at) VALUES
(1, 1, '중량', true, 0, NOW(), NOW());
INSERT INTO courier_product_options (id, option_group_id, name, additional_price, sort_order, stock, created_at, updated_at) VALUES
(1, 1, '2kg (기본)', 0.00, 0, NULL, NOW(), NOW()),
(2, 1, '3kg', 8000.00, 1, 30, NOW(), NOW()),
(3, 1, '5kg', 18000.00, 2, 15, NOW(), NOW());

-- 배 선물세트: 포장 옵션
INSERT INTO courier_product_option_groups (id, courier_product_id, name, required, sort_order, created_at, updated_at) VALUES
(2, 2, '포장', true, 0, NOW(), NOW());
INSERT INTO courier_product_options (id, option_group_id, name, additional_price, sort_order, stock, created_at, updated_at) VALUES
(4, 2, '일반 포장', 0.00, 0, NULL, NOW(), NOW()),
(5, 2, '고급 보자기 포장', 5000.00, 1, 20, NOW(), NOW());

-- 애플망고: 중량 옵션
INSERT INTO courier_product_option_groups (id, courier_product_id, name, required, sort_order, created_at, updated_at) VALUES
(3, 4, '중량', true, 0, NOW(), NOW());
INSERT INTO courier_product_options (id, option_group_id, name, additional_price, sort_order, stock, created_at, updated_at) VALUES
(6, 3, '1kg (2~3과)', 0.00, 0, 20, NOW(), NOW()),
(7, 3, '2kg (4~6과)', 28000.00, 1, 10, NOW(), NOW());

-- 샤인머스캣: 중량 + 포장
INSERT INTO courier_product_option_groups (id, courier_product_id, name, required, sort_order, created_at, updated_at) VALUES
(4, 5, '중량', true, 0, NOW(), NOW()),
(5, 5, '포장', false, 1, NOW(), NOW());
INSERT INTO courier_product_options (id, option_group_id, name, additional_price, sort_order, stock, created_at, updated_at) VALUES
(8, 4, '2kg (2송이)', 0.00, 0, NULL, NOW(), NOW()),
(9, 4, '4kg (4송이)', 25000.00, 1, 15, NOW(), NOW()),
(10, 5, '일반 박스', 0.00, 0, NULL, NOW(), NOW()),
(11, 5, '선물 포장', 3000.00, 1, NULL, NOW(), NOW());

-- 프리미엄 바구니: 사이즈
INSERT INTO courier_product_option_groups (id, courier_product_id, name, required, sort_order, created_at, updated_at) VALUES
(6, 9, '구성', true, 0, NOW(), NOW());
INSERT INTO courier_product_options (id, option_group_id, name, additional_price, sort_order, stock, created_at, updated_at) VALUES
(12, 6, '스탠다드 (4종)', 0.00, 0, 10, NOW(), NOW()),
(13, 6, '프리미엄 (6종)', 30000.00, 1, 5, NOW(), NOW()),
(14, 6, '로얄 (8종)', 60000.00, 2, 0, NOW(), NOW());

-- 8. courier_product_detail_images (상세 이미지)
INSERT INTO courier_product_detail_images (courier_product_id, image_url, sort_order, created_at, updated_at) VALUES
(1, 'products/chamoe-detail-1.jpg', 0, NOW(), NOW()),
(1, 'products/chamoe-detail-2.jpg', 1, NOW(), NOW()),
(2, 'products/naju-pear-detail-1.jpg', 0, NOW(), NOW()),
(5, 'products/shine-muscat-detail-1.jpg', 0, NOW(), NOW()),
(5, 'products/shine-muscat-detail-2.jpg', 1, NOW(), NOW()),
(9, 'products/premium-basket-detail-1.jpg', 0, NOW(), NOW()),
(9, 'products/premium-basket-detail-2.jpg', 1, NOW(), NOW()),
(9, 'products/premium-basket-detail-3.jpg', 2, NOW(), NOW());
