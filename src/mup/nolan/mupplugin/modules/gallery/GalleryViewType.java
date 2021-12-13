package mup.nolan.mupplugin.modules.gallery;

public enum GalleryViewType
{
	MAIN(null),
	BORDER(MAIN),
	BORDER_BUY(BORDER),
	SLOT(MAIN);

	private final GalleryViewType parent;

	GalleryViewType(GalleryViewType parent)
	{
		this.parent = parent;
	}

	public GalleryViewType getParent()
	{
		return parent;
	}

	public boolean canClose()
	{
		return parent == null;
	}

	public boolean isSubmenu()
	{
		return this == BORDER_BUY || this == SLOT;
	}
}
