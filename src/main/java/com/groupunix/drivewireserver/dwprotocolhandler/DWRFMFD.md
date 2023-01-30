# RFM File Descriptor Format

The file descriptor is a sector that is present for every file
on an RBF device. It contains attributes, modification dates,
and segment information on a file.

```
               ORG       0
FD.ATT         RMB       1                   Attributes
FD.OWN         RMB       2                   Owner
FD.DAT         RMB       5                   Date last modified
FD.LNK         RMB       1                   Link count
FD.SIZ         RMB       4                   File size
FD.Creat       RMB       3                   Segment list extension
FD.SEG         EQU       .                   Beginning of segment list
```

## Segment List Entry Format

```
               ORG       0
FDSL.A         RMB       3                   Segment beginning physical sector number
FDSL.B         RMB       2                   Segment size
FDSL.S         EQU       .                   Segment list entry size
FD.LS1         EQU       FD.SEG+((256-FD.SEG)/FDSL.S-1)*FDSL.S
FD.LS2         EQU       (256/FDSL.S-1)*FDSL.S
MINSEC         SET       16
```
