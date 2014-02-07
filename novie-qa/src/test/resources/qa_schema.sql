-- Copyright (c) 2013, Groupon, Inc.
-- All rights reserved.
-- 
-- Redistribution and use in source and binary forms, with or without
-- modification, are permitted provided that the following conditions are
-- met:
-- 
-- Redistributions of source code must retain the above copyright notice,
-- this list of conditions and the following disclaimer.
-- 
-- Redistributions in binary form must reproduce the above copyright
-- notice, this list of conditions and the following disclaimer in the
-- documentation and/or other materials provided with the distribution.
-- 
-- Neither the name of GROUPON nor the names of its contributors may be
-- used to endorse or promote products derived from this software without
-- specific prior written permission.
-- 
-- THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
-- IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
-- TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
-- PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
-- HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
-- SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
-- TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
-- PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
-- LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
-- NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
-- SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


-- drop schema if exists test cascade;
-- create schema test;

-- User dimension table
CREATE TABLE IF NOT EXISTS dim_user (
    u_id INTEGER NOT NULL,
    u_firstname varchar(25),
    u_surname varchar(25),
    u_country varchar(2),
    u_birth_date date,
    PRIMARY KEY (u_id));

-- Application dimension table
CREATE TABLE IF NOT EXISTS dim_app (
    a_id INTEGER NOT NULL,
    a_name varchar(255),
    a_url varchar(255),
    PRIMARY KEY (a_id));

-- Application dimension table
CREATE TABLE IF NOT EXISTS dim_dt (
    dt_id INTEGER NOT NULL,
    dt_name varchar(255),
    PRIMARY KEY (dt_id));
    
-- Application dimension table
CREATE TABLE IF NOT EXISTS dim_dt_CST (
    dt_id INTEGER NOT NULL,
    dt_name varchar(255),
    PRIMARY KEY (dt_id));
   
-- Fact table    
CREATE TABLE IF NOT EXISTS fact (
    u_id INTEGER NOT NULL,
    a_id INTEGER NOT NULL,
    dt_id INTEGER NOT NULL,
    login_succeed INTEGER NOT NULL,
    login_failed INTEGER NOT NULL,
    logon_duration INTEGER NOT NULL,
    PRIMARY KEY (u_id,a_id,dt_id));
   