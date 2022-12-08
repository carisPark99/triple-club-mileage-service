alter table user_point_bas add index user_point_bas_1IDX (user_id);

alter table user_point_detail add index user_point_detail_1IDX (user_point_id);
alter table user_point_detail add index user_point_detail_2IDX (review_id);
alter table user_point_detail add index user_point_detail_3IDX (user_point_id, review_id);

alter table review_bas add index review_bas_1IDX (user_id);
alter table review_bas add index review_bas_2IDX (place_id);
alter table review_bas add index review_bas_3IDX (user_id, place_id);

alter table review_photo_bas add index review_photo_bas_1IDX (photo_id);
alter table review_photo_bas add index review_photo_bas_2IDX (review_id);
alter table review_photo_bas add index review_photo_bas_3IDX (photo_id, review_id);
