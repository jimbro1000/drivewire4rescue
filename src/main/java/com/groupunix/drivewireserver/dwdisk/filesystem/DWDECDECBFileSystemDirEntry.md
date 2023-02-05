# Byte Description

| Offset | Field              | Description                                                                                 |
|--------|:-------------------|:--------------------------------------------------------------------------------------------|
| 0-7    | Filename           | left justified and blank, filled                                                            |
|        |                    | If byte 0 is 0 then the file has been 'KILL'ed and the directory entry is available for use |
|        |                    | If byte 0 is $FF, then the entry and all following entries have never been used             |
| 8-10   | Filename extension |                                                                                             |
| 11     | File type          | 0=BASIC, 1=BASIC data, 2=Machine language, 3=Text editor source                             |
| 12     | ASCII flag         | 0=binary or crunched BASIC, $FF=ASCII                                                       |
| 13     | First granule      | Number of the first granule in the file                                                     |
| 14-15  | Sector Size        | Number of bytes used in the last sector of the file                                         |
| 16-31  | Unused             | (future use)                                                                                |
