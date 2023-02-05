# RBF File system ID Sector Attributes #

| Attribute | Offset | Type | Length | Description                                                              |
|:----------|:------:|------|:------:|:-------------------------------------------------------------------------|
| DD.TOT    |  $00   | int  |   3    | Number of sectors on disk                                                |
| DD.TKS    |  $03   | int  |   1    | Track size (in sectors)                                                  |
| DD.MAP    |  $04   | int  |   2    | Number of bytes in the allocation bit map                                |
| DD.BIT    |  $06   | int  |   2    | Number of sectors per cluster                                            |
| DD.DIR    |  $08   | int  |   3    | Starting sector of the root directory                                    |
| DD.OWN    |  $0B   | int  |   2    | Owner's user number                                                      |
| DD.ATT    |  $0D   | int  |   1    | Disk attributes                                                          |
| DD.DSK    |  $0E   | int  |   2    | Disk identification (for internal use)                                   |
| DD.FMT    |  $10   | int  |   1    | Disk format, density, number of sides                                    |
| DD.SPT    |  $11   | int  |   2    | Number of sectors per track                                              |
| DD.RES    |  $13   | int  |   2    | Reserved for future use                                                  |
| DD.BT     |  $15   | int  |   3    | Starting sector of the bootstrap file                                    |
| DD.BSZ    |  $18   | int  |   2    | Size of the bootstrap file (in bytes)                                    |
| DD.DAT    |  $1A   | int  |   5    | Time of creation (Y:M:D:H:M)                                             |
| DD.NAM    |  $1F   | str  |   32   | Volume name in which the last character has the most significant bit set |
| DD.OPT    |  $3F   | int  |   1    | Path descriptor options                                                  |
