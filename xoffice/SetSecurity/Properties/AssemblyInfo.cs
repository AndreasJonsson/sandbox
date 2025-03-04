﻿using System;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Security.Permissions;

// General Information about an assembly is controlled through the following 
// set of attributes. Change these attribute values to modify the information
// associated with an assembly.
[assembly: AssemblyTitle("InstallerSecurity")]
[assembly: AssemblyDescription("Custul setup action to grant security rights to the add-in.")]
[assembly: AssemblyConfiguration("")]
[assembly: AssemblyCompany("XWiki.org")]
[assembly: AssemblyProduct("XWiki Office")]
[assembly: AssemblyCopyright("Copyright © XWiki.org 2009")]
[assembly: AssemblyTrademark("XWiki")]
[assembly: AssemblyCulture("")]

[assembly: CLSCompliant(true)]
[assembly: System.Security.Permissions.PermissionSet(System.Security.Permissions.SecurityAction.RequestMinimum, Name = "FullTrust")]

// Setting ComVisible to false makes the types in this assembly not visible 
// to COM components.  If you need to access a type in this assembly from 
// COM, set the ComVisible attribute to true on that type.
[assembly: ComVisible(false)]

// Version information for an assembly consists of the following four values:
//
//      Major Version
//      Minor Version 
//      Build Number
//      Revision
//
[assembly: AssemblyVersion("1.0.0.0")]
[assembly: AssemblyFileVersion("1.0.0.0")]
