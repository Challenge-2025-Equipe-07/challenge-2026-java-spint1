-- Modelo fisico do Veti (Oracle Database 23ai), gerado pelo Hibernate a partir das entidades JPA.
-- Nao e executado pela aplicacao: as tabelas sao criadas pelo Hibernate (ddl-auto=update).
-- Objetos PL-SQL (PKG_VETI, TRG_AUDITORIA_CONSULTA) e TB_AUDITORIA: src/main/resources/db/pkg_veti.sql
-- Para regenerar: ver "Modelo de dados" no README.

    create sequence sq_consulta start with 1 increment by 1;

    create sequence sq_exame start with 1 increment by 1;

    create sequence sq_exame_medicamento start with 1 increment by 1;

    create sequence sq_medicamento start with 1 increment by 1;

    create sequence sq_pet start with 1 increment by 1;

    create sequence sq_receita start with 1 increment by 1;

    create sequence sq_receita_medicamento start with 1 increment by 1;

    create sequence sq_tutor start with 1 increment by 1;

    create sequence sq_veterinario start with 1 increment by 1;

    create table tb_consulta (
        dt_consulta timestamp(9) not null,
        id_consulta number(19,0) not null,
        tb_pet_id_pet number(19,0) not null,
        tb_veterinario_id_veterinario number(19,0) not null,
        primary key (id_consulta),
        constraint TB_CON_DT_VET_UN unique (dt_consulta, tb_veterinario_id_veterinario)
    );

    create table tb_exame (
        id_exame number(19,0) not null,
        tb_consulta_id_consulta number(19,0) not null,
        ds_exame varchar2(100 char) not null,
        ds_url varchar2(500 char),
        primary key (id_exame)
    );

    create table tb_exame_medicamento (
        qt_medicamento number(10,0) not null check ((qt_medicamento>=1) and (qt_medicamento<=99)),
        id_pedido number(19,0) not null,
        tb_exame_id_exame number(19,0) not null,
        tb_medicamento_id_medicamento number(19,0) not null,
        ds_posologia varchar2(1000 char) not null,
        primary key (id_pedido)
    );

    create table tb_medicamento (
        id_medicamento number(19,0) not null,
        nm_medicamento varchar2(100 char) not null,
        ds_medicamento varchar2(400 char) not null,
        primary key (id_medicamento)
    );

    create table tb_pet (
        ds_idade number(10,0) not null check ((ds_idade<=99) and (ds_idade>=0)),
        ds_peso NUMBER(5,2) not null,
        fl_castrado boolean not null,
        id_pet number(19,0) not null,
        tb_tutor_id_tutor number(19,0) not null,
        ds_raca varchar2(100 char) not null,
        nm_pet varchar2(100 char) not null,
        primary key (id_pet)
    );

    create table tb_receita (
        id_receita number(19,0) not null,
        tb_consulta_id_consulta number(19,0) not null,
        ds_receita varchar2(500 char) not null,
        primary key (id_receita)
    );

    create table tb_receita_medicamento (
        qt_medicamento number(10,0) not null check ((qt_medicamento>=1) and (qt_medicamento<=99)),
        id_pedido number(19,0) not null,
        tb_medicamento_id_medicamento number(19,0) not null,
        tb_receita_id_receita number(19,0) not null,
        ds_posologia varchar2(1000 char) not null,
        primary key (id_pedido)
    );

    create table tb_tutor (
        id_tutor number(19,0) not null,
        ds_cpf varchar2(11 char) not null,
        ds_telefone varchar2(13 char),
        nm_tutor varchar2(300 char) not null,
        ds_email varchar2(320 char) not null,
        primary key (id_tutor),
        constraint TB_TUTOR_DS_CPF_UN unique (ds_cpf),
        constraint TB_TUTOR_DS_EMAIL_UN unique (ds_email)
    );

    create table tb_veterinario (
        id_veterinario number(19,0) not null,
        ds_role varchar2(20 char) not null,
        ds_password varchar2(100 char) not null,
        ds_email varchar2(320 char) not null,
        primary key (id_veterinario),
        constraint TB_VETERINARIO_DS_EMAIL_UN unique (ds_email)
    );

    create index TB_RECEITA__IDX 
       on tb_receita (tb_consulta_id_consulta);

    alter table if exists tb_consulta 
       add constraint TB_CONSULTA_TB_PET_FK 
       foreign key (tb_pet_id_pet) 
       references tb_pet;

    alter table if exists tb_consulta 
       add constraint TB_CON_TB_VET_FK 
       foreign key (tb_veterinario_id_veterinario) 
       references tb_veterinario;

    alter table if exists tb_exame 
       add constraint TB_EXM_TB_CON_FK 
       foreign key (tb_consulta_id_consulta) 
       references tb_consulta;

    alter table if exists tb_exame_medicamento 
       add constraint TB_EXM_MED_EXM_FK 
       foreign key (tb_exame_id_exame) 
       references tb_exame;

    alter table if exists tb_exame_medicamento 
       add constraint TB_EXM_MDCNT_MDCNT_FK 
       foreign key (tb_medicamento_id_medicamento) 
       references tb_medicamento;

    alter table if exists tb_pet 
       add constraint TB_PET_TB_TUTOR_FK 
       foreign key (tb_tutor_id_tutor) 
       references tb_tutor;

    alter table if exists tb_receita 
       add constraint TB_RCT_CON_FK 
       foreign key (tb_consulta_id_consulta) 
       references tb_consulta;

    alter table if exists tb_receita_medicamento 
       add constraint TB_RCT_MDCMNT_FK 
       foreign key (tb_medicamento_id_medicamento) 
       references tb_medicamento;

    alter table if exists tb_receita_medicamento 
       add constraint TB_MEDICAMENTO_RECEITA_FK 
       foreign key (tb_receita_id_receita) 
       references tb_receita;
