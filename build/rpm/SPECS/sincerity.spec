Name:           sincerity
Summary:        Sincerity is a container and dependency manager for the JVM.
Version:        1.0beta1
Release:        0
Group:          Three Crickets
License:        LGPLv3+

%description 
Sincerity is a container and dependency manager for the JVM.

%prep

%build

%clean 

%install

%post
ln -fs /usr/lib/sincerity/sincerity /usr/bin/sincerity

%preun
rm -f /usr/bin/sincerity

%files
/*

%changelog
* Thu May 10 2012 Tal Liron <tal.liron@threecrickets.com>
- Initial release
