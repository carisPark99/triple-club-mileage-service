use triple;

# -----------------------------------------------------------------------------------------#
#                                        사용자 테이블
# -----------------------------------------------------------------------------------------#
drop table user_bas;
create table user_bas
(
    id binary(16) not null primary key comment '사용자ID',
    name varchar(20) not null comment '사용자이름',
    create_dt datetime comment '생성일시',
    update_dt datetime comment '변경일시'
)
;
# -----------------------------------------------------------------------------------------#
#                                      사용자 포인트 테이블
# -----------------------------------------------------------------------------------------#
drop table user_point_bas;
create table user_point_bas
(
    id binary(16) not null primary key comment '사용자포인트ID',
    user_id binary(16) not null unique comment '사용자ID',
    review_tot_point int default 0 comment '리뷰총점수',
    create_dt datetime comment '생성일시',
    update_dt datetime comment '변경일시',
        constraint fk_user_id foreign key (user_id) references user_bas (id)
)
;
# -----------------------------------------------------------------------------------------#
#                                      사용자 포인트 상세 테이블
# -----------------------------------------------------------------------------------------#
drop table user_point_detail;
create table user_point_detail
(
    id binary(16) not null primary key comment '사용자포인트상세ID',
    user_point_id binary(16) not null comment '사용자포인트ID',
    review_id binary(16) default 0 comment '리뷰ID',
    review_point int default 0 comment '리뷰포인트점수',
    mod_review_point int default 0 comment '리뷰차감점수',
    create_dt datetime comment '생성일시',
    update_dt datetime comment '변경일시',
    constraint fk_user_point_id foreign key (user_point_id) references user_point_bas (id)
)
;
# -----------------------------------------------------------------------------------------#
#                                      리뷰 테이블
# -----------------------------------------------------------------------------------------#
drop table review_bas;
create table review_bas
(
    id binary(16) not null primary key comment '리뷰ID',
    user_id binary(16) not null comment '사용자ID',
    place_id binary(16) not null comment '장소ID',
    content varchar(1000) comment '리뷰내용',
    create_dt datetime comment '생성일시',
    update_dt datetime comment '변경일시',
    constraint fk_review_user_id foreign key (user_id) references user_bas (id)
)
;
# -----------------------------------------------------------------------------------------#
#                                      장소 테이블
# -----------------------------------------------------------------------------------------#
drop table place_bas;
create table place_bas
(
    id binary(16) not null primary key comment '장소ID',
    name varchar(100) comment '장소명',
    create_dt datetime comment '생성일시',
    update_dt datetime comment '변경일시'
)
;
# -----------------------------------------------------------------------------------------#
#                                      리뷰 사진 테이블
# -----------------------------------------------------------------------------------------#
drop table review_photo_bas;
create table review_photo_bas
(
    id binary(16) not null primary key comment '리뷰사진ID',
    photo_id binary(16) comment '사진ID',
    review_id binary(16) not null comment '리뷰ID',
    name varchar(100) comment '사진이름',
    create_dt datetime comment '생성일시',
    update_dt datetime comment '변경일시',
        constraint fk_review_id foreign key (review_id) references review_bas (id)
)
;
# -----------------------------------------------------------------------------------------#
#                                      End
# -----------------------------------------------------------------------------------------#
