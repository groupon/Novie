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


delete from dim_user;

insert into dim_user values(1, 'Martin','Smith','US','1999-01-06');
insert into dim_user values(2, 'Thomas A.','Anderson','US','1999-05-16');
insert into dim_user values(3, 'Roy', 'Trenneman','UK','1985-08-06');
insert into dim_user values(4, 'Maurice', 'Moss','UK','1986-10-26');
insert into dim_user values(5, 'Jen', 'Barber','UK','1983-02-21');
insert into dim_user values(6, 'Denholm', 'Reynholm','UK','1967-07-08');
insert into dim_user values(7, 'Null user', null,'UK','1967-07-08');

delete from dim_app;

insert into dim_app values(1,'Groupon test','http://test.groupon.com/');
insert into dim_app values(2,'Apache test','http://test.apache.org/logon');
insert into dim_app values(3,'github','https://github.com/');

delete from dim_dt;

insert into dim_dt values(1,'2013-07-01T00:00');
insert into dim_dt values(2,'2013-07-01T01:00');

delete from dim_dt_CST;

insert into dim_dt_CST values(1,'2013-06-30T19:00');
insert into dim_dt_CST values(2,'2013-06-30T20:00');

delete from fact;

-- u_id,a_id,dt_id,succeed,failed,duration
-- 2013-07-01T00:00 UTC => 2013-06-30T19:00 CST
insert into fact values(1,1,1,3,0,25);
insert into fact values(1,2,1,2,1,10);
insert into fact values(2,2,1,1,0,2);
insert into fact values(3,1,1,5,0,50);
insert into fact values(3,2,1,2,0,25);
insert into fact values(3,3,1,3,1,15);
insert into fact values(4,1,1,3,2,25);
insert into fact values(4,2,1,1,0,5);
insert into fact values(5,1,1,25,10,250);
insert into fact values(6,1,1,1,17,120);
insert into fact values(7,1,1,1,17,120);
-- 2013-07-01T01:00 UTC => 2013-06-30T20:00 CST
insert into fact values(1,1,2,3,0,25);
insert into fact values(1,2,2,2,1,10);
insert into fact values(2,2,2,1,0,2);
insert into fact values(3,1,2,5,0,50);
insert into fact values(3,2,2,2,0,25);
insert into fact values(3,3,2,3,1,15);
insert into fact values(4,1,2,3,2,25);
insert into fact values(4,2,2,1,0,5);
insert into fact values(5,1,2,25,10,250);
insert into fact values(6,1,2,1,17,120);
insert into fact values(7,1,2,1,17,120);


