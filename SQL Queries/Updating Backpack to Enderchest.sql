SELECT DISTINCT name, COUNT(*) as count FROM minecraft.permissions where permission LIKE "betterenderchest.slots%" group by name;
SELECT * from minecraft.permissions where name="DatWoothie" or name="Loart" or name="michealv" or name="oldskoolish" or name="SteelMcArthur" or name="Xakorik" or name="Zenreich";
SELECT * FROM towny.permissions_backup WHERE permission like "%upgrade2%";
INSERT INTO minecraft.permissions (name,type,permission,world,value) 
   SELECT DISTINCT p1.name, 1 as type, "betterenderchest.slots.upgrade1" as permission, "" as world, "" as value from minecraft.permissions as p1 JOIN towny.permissions_backup as p2 on p1.name=p2.name WHERE p2.permission="backpack.size.54" AND p1.name NOT IN (SELECT name from minecraft.permissions where permission="betterenderchest.slots.upgrade1" or permission="betterenderchest.slots.upgrade2");
SELECT *  from minecraft.permissions as p1 JOIN towny.permissions_backup as p2 on p1.name=p2.name WHERE p1.permission="betterenderchest.slots.upgrade1" and p2.permission="backpack.size.54";
UPDATE minecraft.permissions SET permission="betterenderchest.slots.upgrade2" WHERE permission="betterenderchest.slots.upgrade1" AND name IN (SELECT name FROM towny.permissions_backup WHERE permission like "%upgrade2%");