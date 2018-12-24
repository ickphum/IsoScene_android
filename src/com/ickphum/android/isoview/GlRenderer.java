package com.ickphum.android.isoview;

import javax.microedition.khronos.egl.EGLConfig;

import javax.microedition.khronos.opengles.GL10;

import com.ickphum.android.isoscene.R;
import com.ickphum.android.isoview.IsoFrame.Action;
import com.ickphum.android.isoview.IsoFrame.CubeSide;
import com.ickphum.android.isoview.IsoFrame.TileShape;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GlRenderer implements Renderer {

	// 
	public class GridTriangle {
		public int left, top, right;
		public CubeSide facing; 
		public GridTriangle(int left, int top, CubeSide facing) {
			this.left = left;
			this.top = top;
			this.right = left + top;
			this.facing = facing;
			Log.d("GridTriangle", this.dump());
		}
		public String dump () {
			return " GT: " + left + "," + top + "," + right + "," + facing;
		}
		public String toString() {
			return facing + "_" + left + "_" + top + "_" + right;
		}
	}

	// define sectorSize as 0 so if we fail to load a config, we'll crash with div0 
	// the first time we try to create a key
	static int sectorSize = 0;
	public class SectorKey {
		public int column, row;
		
		public SectorKey (int left, int top) {
			
			// left and top are iso grid coords; convert them to rectangular sector coords
	        column = (int) Math.floor((left-1) / sectorSize);
	        row = (int) Math.floor(top / sectorSize);
		}
		public String toString() {
			return " SK: " + column + "," + row;
		}
	}
	
	public class Tile {
		public GridTriangle mAddress;
		public TileShape mShape;
		public int mBrushIndex;
		public SectorKey mSectorKey;
		
		public Tile(GridTriangle address, TileShape shape, int brushIndex) {
			Log.d("Tile", "creating tile at " + address);
			mAddress = address;
			mShape = shape;
			mBrushIndex = brushIndex;
			mSectorKey = new SectorKey(address.left, address.top);
		}
	}
	
	public class Sector {
		private RenderGroup mShape;
		private RenderGroup mText;
		private boolean mDirty;
		private Map<GridTriangle, Boolean> mTile;

		public Sector (SectorKey key) {
			Log.d("Sector", "creating sector at " + key);
			mKey = key;
			mShape = new RenderGroup("SS:" + mKey.column + ',' + mKey.row, GL10.GL_TRIANGLES);
			mText = new RenderGroup("ST:" + mKey.column + ',' + mKey.row, GL10.GL_TRIANGLES, R.drawable.font_trans);
			mTile = new HashMap<GridTriangle, Boolean>();
			mSector.put(mKey.toString(), this);
		}

		public SectorKey mKey;

		public void draw(GL10 gl) {
			mShape.draw(gl);
			mText.draw(gl);
		}

		public void finish() {
			mShape.finishBuffers();
			mText.finishBuffers();
			mDirty = false;
		}

		public boolean dirty() {
			return mDirty;
		}
		
		public void addTile(GridTriangle address) {
			mTile.put(address, true);
			mDirty = true;
		}

		public void setDirty(boolean flag) {
			mDirty = flag;
		}
	}
	
	private ArrayList<SectorKey> mVisibleSectors = new ArrayList<SectorKey>();
	//private Sector[][] mSector = {};
    private HashMap<String, Sector> mSector = new HashMap<String, Sector>();
	private HashMap<GridTriangle, Tile> mGrid = new HashMap<GridTriangle, Tile>();
	private HashMap<GridTriangle, Tile> mTransientGrid = new HashMap<GridTriangle, Tile>();
    //private Triangle    triangle;   // the square to be drawn
	private float mOriginX = 0;
    private float mOriginY = 0;
    private float mScale = 0.5f;
    private int mDeviceWidth;
    private int mDeviceHeight;

    private float mXGridSize;
    private float mYGridSize;
    private float mControlGradient;
    private Context context;
    private int[] textures;
    //private HashMap<Integer,Integer> texture = new HashMap<Integer, Integer>();
    private SparseIntArray texture = new SparseIntArray();
    private ArrayList<Triangle> triangles = new ArrayList<Triangle>();
    private RenderGroup gridRG = new RenderGroup("gridRG", GL10.GL_LINES, Arrays.asList(0xff,0xff,0xff,0xff));
    private RenderGroup originRG = new RenderGroup("originRG", GL10.GL_LINES, Arrays.asList(0xff,0,0,0xff));
    HashMap<String, Boolean> mPreviousDirtySector = new HashMap<String, Boolean>();
	private IsoFrame frame;

    // Constructor 
    public GlRenderer(Context context) {
    	this.context = context;
    	calculateGridDims(57.735f);
    }

    private void calculateGridDims(float side) {
    	//Log.d("calculateGridDims", "cos 30 = " + )
    	mXGridSize = (float) Math.cos(Math.toRadians(30)) * side;
    	mYGridSize = side;		
    	mControlGradient = (mYGridSize / 2) / mXGridSize; 
    	Log.d("calculateGridDims", "grid size = " + mXGridSize + "x" + mYGridSize + ", gradient = " + mControlGradient);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig egl) {
        // Set the background frame color
    	gl.glClearColor(0.3f, 0.3f, 0.6f, 1.0f);
    	
    	// list of bitmap resource IDs
    	int[] textureResources = {
    		R.drawable.hamish,
    		R.drawable.font_trans,
    		R.drawable.select_texture,
    	};

    	// generate that many texture pointers
        textures = new int[textureResources.length];
    	gl.glGenTextures(textureResources.length, textures, 0);
    	
    	// assign bitmaps to textures
    	for (int i = 0; i < textureResources.length; i++) {

	    	// select next texture id
	    	gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
	
	    	// create nearest filtered texture
	    	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
	    	gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	    	
	    	gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
	    		     
	    	// Use Android GLUtils to specify a two-dimensional texture image from our bitmap
    		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), textureResources[i]);
	    	GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

	    	// Clean up
	    	bitmap.recycle();
	    	
	    	// map the texture resource to the id
	    	texture.put(textureResources[i], textures[i]);
    	}

    	addTriangle();  	
    	
    	originRG.addVertices(1,1,   1,-1);
    	originRG.addVertices(1,-1,  -1,-1);
    	originRG.addVertices(-1,-1, -1,1);
    	originRG.addVertices(-1,1,  1,1);
    	originRG.finishBuffers();
    	
    	// transparent textures
    	gl.glEnable(GL10.GL_BLEND);
    	gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

    }
	
	public void addTriangle() {
		triangles.add(new Triangle(this));
	}

	public GridTriangle addTile(float deviceX, float deviceY, TileShape shape) {
		// device position is reported from top mColumn, OpenGL works from bottom mColumn
		deviceY = mDeviceHeight - deviceY;
		
		// work out logical position using the old scale
		float logicalX = (int)(deviceX * mScale - this.mOriginX);
		float logicalY = (int)(deviceY * mScale - this.mOriginY);
		
		GridTriangle t = this.findGridTriangle(logicalX, logicalY);
		
		return addTile(t, shape, (Integer)frame.getCurrentColor());
	}

	public GridTriangle addTile(GridTriangle t, TileShape shape, Integer brushIndex) {
		
		Log.d("addTile", t.dump());
		
	    // change the grid key according to the shape if we're in the wrong triangle;
	    if (shape == TileShape.LEFT && t.facing == CubeSide.LEFT) {
	        t.left++;
	        t.right++;
	    }
	    else if (shape == TileShape.TOP && t.facing == CubeSide.RIGHT) {
	        t.right--;
	        t.top--;
	    }

	    // just ensure the facing is right, don't bother checking it
	    t.facing = IsoFrame.shapeFacing.get(shape);
	    
	    // check for the same shape in the same place; we may just change the colour of that tile
	    // if we find it.
	    Tile existingTile = mGrid.get(t);
	    if (existingTile != null) {
	        if (existingTile.mShape == shape) {
	        	
	            if (frame.getConfig().getBoolean("repaint_same_tile")) {
	            	
	                existingTile.mBrushIndex = brushIndex != null 
	                	? brushIndex 
	                	: frame.getCurrentColor();
	                mSector.get(existingTile.mSectorKey).setDirty(true);
	                return existingTile.mAddress;
	            }
	            else {
	                return null;
	            }           
	        }
	    }
	    
	    Action action = frame.getAction();

	    // check for existing tiles that overlap.
	    boolean doingShadeArea = action == IsoFrame.Action.SHADE_CUBE && frame.getAreaMode();
	    int shifts = 0;
	    int[][] clashTriangles = IsoFrame.shapeClashTriangles[shape.getInt()];
	    int[][][] clashOffsets = IsoFrame.shapeClashOffsets[shape.getInt()];
	    for (int i=0; i<clashTriangles.length; i++) {
	    
	    	int shiftFlag = clashTriangles[i][4];
	    	int[][] clashList = clashOffsets[i];
	    	Log.d("addTile", "clashList.length = " + clashList.length);
    	    for (int j=0; j<clashList.length; j++) {

    	    	Log.d("addTile", "check clash at " + i + ", " + j);
    	    	TileShape clashShape = TileShape.fromInt(clashList[j][3]);
	            GridTriangle gridKey = new GridTriangle(
	            	t.left + clashList[j][0],
	            	t.top + clashList[j][1],
	            	IsoFrame.shapeFacing.get(clashShape));

	            if ((existingTile = mGrid.get(gridKey)) != null) {
	                if (existingTile.mShape == clashShape) {
	                    shifts += shiftFlag;
	                    break;
	                }
	            }

	            // if we're shading an area, we have to check for clashes in transient_grid as well
	            else {
	            	if (doingShadeArea && ((existingTile = mTransientGrid.get(gridKey)) != null) ) {
	            
		                if (existingTile.mShape == clashShape) {
		                    shifts += shiftFlag;
		                    break;
		                }
	            	}
	            }
	        }
	    }
	    
	    // check what clashes we found
	    if (shifts == 1 || shifts == 2) {

	        // clash with one point only, so shift to a new shape (ie a triangle) as required
	    	// shifts is the index (+1) of the offset list we need to adjust the anchor
	    	shifts--;

	        t.left += clashTriangles[shifts][0];
	        t.top += clashTriangles[shifts][1];
	        t.right += clashTriangles[shifts][2];
	        shape = TileShape.fromInt(clashTriangles[shifts][3]);
	        t.facing = IsoFrame.shapeFacing.get(shape);
	    }
	    else {
	    	if (shifts == 3) {
	    		Log.d("addTile", "clashes with existing cell(s)");
		        return null;
	    	}
	    }
	    
	    // create a tile with key 't' and shape 'shape'
	    Tile tile = new Tile(t, shape, brushIndex);

	    // can only do this with a completed tile
	    Sector sector;
	    if ((sector = mSector.get(tile.mSectorKey)) == null) {
	    	sector = new Sector(tile.mSectorKey);
	    }

	    // TODO I think this covers all cases when we should use transient_grid but check it
	    // if (mAreaStart || action == IsoFrame.Action.PASTE) {
	    if (action == IsoFrame.Action.PASTE) {
	        mTransientGrid.put(t, tile);
	    }
	    else {
	        mGrid.put(t, tile);
	        sector.addTile(t);
	    }


		return t;
	}
	
	@Override
	public void onSurfaceChanged (GL10 gl, int width, int height) {
		if(height == 0) {                       //Prevent A Divide By Zero By
			height = 1;                         //Making Height Equal One
		}

		this.mDeviceWidth = width;
		this.mDeviceHeight = height;

		Log.d("onSurfaceChanged", "width " + width + ", height " + height);
		gl.glViewport(0, 0, width, height);     //Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION);    //Select The Projection Matrix
		gl.glLoadIdentity();                    //Reset The Projection Matrix

		gl.glOrthof(0f, width * mScale, 0f, height * mScale, 0.0f, 20.0f);
		//gl.glOrthof(-7.65f, 7.65f, -5f, 5f, 0.0f, 20.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);     //Select The Modelview Matrix
		gl.glLoadIdentity();                    //Reset The Modelview Matrix

		calculateGridPoints();
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		
		//Log.d("onDrawFrame", "test");

		// clear Screen and Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_PROJECTION);    //Select The Projection Matrix
		gl.glLoadIdentity();                    //Reset The Projection Matrix
		gl.glOrthof(0f, this.mDeviceWidth * mScale, 0f, this.mDeviceHeight * mScale, 0.0f, 20.0f);
		
		// Reset the Modelview Matrix; not sure why this is necessary but it doesn't redraw
		// after the first frame without it.
		gl.glMatrixMode(GL10.GL_MODELVIEW);     // Select The Modelview Matrix
		gl.glLoadIdentity();

		// Drawing
		gl.glTranslatef(mOriginX, mOriginY, 0f);     // move 5 units INTO the screen
		// is the same as moving the camera 5 units away
		
		// recall that a Triangle object may be a group of on-screen triangles
		Iterator<Triangle> i = triangles.iterator();
		while (i.hasNext()) {
			Triangle t = (Triangle) i.next();
			t.draw(gl);
		}
		
		if (mScale < 2f)
			gridRG.draw(gl);
		
		HashMap<String, Boolean> dirtySector = new HashMap<String, Boolean>();
		for (SectorKey key : mVisibleSectors) {
			//Log.d("onDrawFrame", "visible sector " + key);
			
			Sector sector = mSector.get(key);
			if (sector != null) {
				Log.d("onDrawFrame", "sector " + key + " is not null");
			
				boolean forceFinalRebuild = ( ! sector.dirty() && mPreviousDirtySector.get(sector.toString()));
				if (sector.dirty() || forceFinalRebuild) {
					if (sector.dirty()) {
						dirtySector.put(sector.toString(), true);
					}
					sector.finish();
				}
				sector.draw(gl);
		
			}
		}
		mPreviousDirtySector = dirtySector;
				
		originRG.draw(gl);
	}
	
	public int getTextureId(int resourceId) {
		return texture.get(resourceId);
	}

	public void moveOrigin(float dx, float dy) {
		mOriginX += dx * mScale;
		mOriginY += dy * mScale;
		Log.d("moveOrigin", "mOriginX " + mOriginX + ", mOriginY " + mOriginY);
		calculateGridPoints();
	}
	
	public void changeScale(float factor, float deviceX, float deviceY) {
		
		// device position is reported from top mColumn, OpenGL works from bottom mColumn
		deviceY = mDeviceHeight - deviceY;
		
		// work out logical position using the old scale
		float logicalX = (int)(deviceX * mScale - this.mOriginX);
		float logicalY = (int)(deviceY * mScale - this.mOriginY);

		mScale /= factor;
		
		mOriginX = (1f/factor) * (this.mOriginX + logicalX) - logicalX;
		mOriginY = (1f/factor) * (this.mOriginY + logicalY) - logicalY;
		
		Log.d("changeScale", "new scale " + mScale
				/*
				+ ", deviceX " + deviceX 
				+ ", deviceY " + deviceY
				+ ", logicalX " + logicalX
				+ ", logicalY " + logicalY
				+ " => mOriginX " + mOriginX
				+ ", mOriginY " + mOriginY
				*/
				);
		calculateGridPoints();
	}
	
	public GridTriangle findGridTriangle(float logicalX, float logicalY) {

	    int minX = (int) Math.floor(logicalX / mXGridSize);
	    int minY = (int) Math.floor(logicalY / mYGridSize);

	    boolean minXIsEven = minX % 2 == 0;

	    float key[] = {
			minXIsEven 
	        	? (minX + 1 ) * mXGridSize
	    	    : minX * mXGridSize, 
	    	(float) ((minY + 0.5) * mYGridSize)
	    };

	    float pointGradient = (key[1] - logicalY)/(key[0] - logicalX);

	    float y_anchor;
	    CubeSide facing;

	    if (Math.abs(pointGradient) < mControlGradient) {
	        if (minXIsEven) {
	            y_anchor = minY + 1;
	            facing = CubeSide.RIGHT;
	        }
	        else {
	            y_anchor = minY + 0.5f;
	            facing = CubeSide.LEFT;
	        }
	    }
	    else {
	        if (pointGradient > 0) {
	            if (minXIsEven) {
	                y_anchor = minY;
	                facing = CubeSide.LEFT;
	            }
	            else {
	                y_anchor = minY + 1.5f;
	                facing = CubeSide.RIGHT;
	            }
	        }
	        else {
	            if (minXIsEven) {
	                y_anchor = minY + 1;
	                facing = CubeSide.LEFT;
	            }
	            else {
	                y_anchor = minY + 0.5f;
	                facing = CubeSide.RIGHT;
	            }
	        }
	    }

//	    int top = minX;
//	    int mColumn = (int) y_anchor - minX / 2;
//	    int right = mColumn + top;

//	    Log.d("find_triangle_coords mColumn, top, right, facing", null);

//	    @stash = (minX, minY, @key, @anchor, facing);

	    GridTriangle triangle = new GridTriangle((int) y_anchor - minX / 2, minX, facing);
	    Log.d("findGridTriangle", "point " + logicalX + "," + logicalY + triangle.dump());
	    return triangle;
	}
	
	private void calculateGridPoints() {

	    //my $scene = $self->scene;

	    float logicalWidth = mDeviceWidth * mScale;
	    float logicalHeight = mDeviceHeight * mScale;
	    float logicalOriginX = 0 - mOriginX;
	    float logicalOriginY = 0 - mOriginY;
	    Log.d("calculateGridPoints", "origin = " + logicalOriginX + ',' + logicalOriginY
	    		+ ", dim = " + logicalWidth + "x" + logicalHeight);

	    // find corner triangles; bottom mColumn to top mColumn, bottom right to top right
	    GridTriangle corners[] = {
	    		findGridTriangle(logicalOriginX, logicalOriginY),
	    		findGridTriangle(logicalOriginX, logicalOriginY + logicalHeight),
	    		findGridTriangle(logicalOriginX + logicalWidth, logicalOriginY),
	    		findGridTriangle(logicalOriginX + logicalWidth, logicalOriginY + logicalHeight)
	    };

	    // adjust the corners and find the corner sectors
	    //ArrayList<SectorKey> cornerSectors = new ArrayList<SectorKey>();
	    //int sectorSize = 20;
	    
	    /*
	    for (int i = 0; i < 4; i++) {
	    	
		    // we want to know the bottom mColumn corner of a right-shaped tile containing the point; that corner
		    // is not the anchor of a right tile, but the sectors do not go by the same system.
		    // Whatever facing triangle we found, we want the anchor point immediately below it, due to the
		    // anchor placement on the triangles. We don't change the facing, we're just after the mColumn and top coords.
	        corners[i].left--;
	        corners[i].right--;
	        //cornerSectors.add(new SectorKey( (int) Math.floor(corners[i].left / sectorSize),
	        //		(int) Math.floor(corners[i].top / sectorSize)));
	    }

	    SectorKey[] cornerSectors = {
	        new SectorKey( (int) Math.floor(corners[0].left / sectorSize), (int) Math.floor(corners[0].top / sectorSize)),
	        new SectorKey( (int) Math.floor(corners[1].left / sectorSize), (int) Math.floor(corners[1].top / sectorSize)),
	        new SectorKey( (int) Math.floor(corners[2].left / sectorSize), (int) Math.floor(corners[2].top / sectorSize)),
	        new SectorKey( (int) Math.floor(corners[3].left / sectorSize), (int) Math.floor(corners[3].top / sectorSize)),
	    };
	    */
	    
	    SectorKey[] cornerSectors = {
	        new SectorKey( corners[0].left, corners[0].top),
	        new SectorKey( corners[1].left, corners[1].top),
	        new SectorKey( corners[2].left, corners[2].top),
	        new SectorKey( corners[3].left, corners[3].top),
	        new SectorKey( (int) Math.floor(corners[2].left / sectorSize), (int) Math.floor(corners[2].top / sectorSize)),
	        new SectorKey( (int) Math.floor(corners[3].left / sectorSize), (int) Math.floor(corners[3].top / sectorSize)),
	    };
	    Log.d("calculateGridPoints", "cornerSectors " + cornerSectors[0]);
	    
	    mVisibleSectors = new ArrayList<SectorKey>();

	    // We're using sectors based on R shaped tiles (arbitrarily chosen over L; T would have been less
	    // useful since they have no sides parallel with the window sides). This means that L and T tiles
	    // on the bottom and right edges respectively may extend past the edges of the sector as made up
	    // of R tiles. What this means in practice is that sectors that are just above or to the mColumn of
	    // the visible area (and technically out of view) may need to be rendered as well in case such tiles exist.
	    // We are vulnerable to this problem when the mColumn edge of the sector is just out of view; when this is the case,
	    // we add the next mColumn to the mColumn as well, as well as the sector above the top corner.
	    if ((logicalOriginX - cornerSectors[0].column * sectorSize * mXGridSize) < mXGridSize) {

	        Log.d("calculateGridPoints","add extra sectors for edge coverage");

	        // add mColumn to mColumn of sector edge
	        for (int left = cornerSectors[0].column; left <= cornerSectors[1].column + 1; left++) {
	            mVisibleSectors.add(new SectorKey(left, (cornerSectors[0].row - 1)));
	        }

	        // add sector above top corner
	        mVisibleSectors.add(new SectorKey(cornerSectors[1].column + 1, cornerSectors[1].row));
	    }
	    
	    Log.d("calculateGridPoints", "mVisibleSectors @ 1 " + mVisibleSectors);

	    // finding the vertical sector list along each edge is easy, we just fill in the gap between the
	    // respective corner pairs. finding the top and bottom edges is not so easy, since the required sectors
	    // will exhibit the usual sawtooth shape along these edges, and the top and bottom sawtooth shapes need not be 
	    // in sync; both will have two tiles with the same L coord, ie ramping up as T increases, then L drops. However,
	    // the points where L drops at top and bottom are not related.
	    // What we need to do (for each of top and bottom) is work out where in the sequence we are at the start; we will drop L after the first or
	    // second tile, and then every two tiles after that as we go across (increasing T by 1 per step).
	    // have to work out the shift from the mColumn corner sectors to the top and bottom sectors in the next mColumn.
	    // For each mColumn corner, the next sector will either be mColumn, top+1 or mColumn-1,top+1. Once we know this, we have the 
	    // size of both even and odd sector strips and we can assemble the sector list.
	    // See sector_edges.jpg for diagrams; the reference line is labelled C.

	    // bottom mColumn; there are two reference lines, 1 and 0.5 Y sector dim above the sector corner.
	    // Below the 0.5 line we're at stage 0 but we need to drop the corner by 1 dim, below
	    // the 1 line we're at stage 1, otherwise stage 0.
	    float topRefLine = (cornerSectors[0].column + cornerSectors[0].row / 2 + 1) * mYGridSize * sectorSize;
	    float bottomRefLine = (cornerSectors[0].column + cornerSectors[0].row / 2 + 0.5f) * mYGridSize * sectorSize;
	    int bottomStage = 0;
	    if (logicalOriginY <= bottomRefLine) {
	        cornerSectors[0].column--;
	    }
	    else {
	    	if (logicalOriginY <= topRefLine) {
		        bottomStage = 2;
		    }
	    }
	    Log.d("calculateGridPoints", "topRefLine " + topRefLine + ", bottomRefLine " + bottomRefLine);
	    	
	    // top mColumn; reference line 0.5 and 1 Y dims above the sector corner. We never change the corner location
	    // in this case, but we may have to start at stage -1 so we get 3 initial raises.
	    bottomRefLine = (cornerSectors[1].column + cornerSectors[1].row / 2 + 0.5f) * mYGridSize * sectorSize;
	    topRefLine = (cornerSectors[1].column + cornerSectors[1].row / 2 + 1) * mYGridSize * sectorSize;
	    float topEdge = logicalOriginY + logicalHeight;
	    int topStage = topEdge < bottomRefLine
	        ? 1
	        : topEdge < topRefLine
	            ? 0
	            : -1;
	    Log.d("calculateGridPoints", String.format("logical_origin_y %f, logical_height %f, top corner %f, bottom_ref_line %f, top ref line %f",
	        logicalOriginY, logicalHeight, logicalOriginY + logicalHeight, bottomRefLine, topRefLine));
	    Log.d("calculateGridPoints","bottom_stage bottomStage, top_stage topStage");

	    // we'll move across the grid, adding all sectors between top and bottom for each 
	    // mColumn of sectors. We adjust the top and bottom coords using the stage fields.
	    // stop once we've done the final mColumn.
	    int sectorBottom = cornerSectors[0].column;
	    int sectorTop = cornerSectors[1].column;
	    for (int sectorColumn = cornerSectors[0].row; sectorColumn <= cornerSectors[2].row; sectorColumn++) {

	        // sectorRow is actually the mColumn coord of the sector
	        for (int sectorRow = sectorBottom; sectorRow <= sectorTop; sectorRow++) {
		        mVisibleSectors.add(new SectorKey(sectorRow, sectorColumn));
	        }

	        bottomStage++;
	        if (bottomStage == 2) {
	            bottomStage = 0;
	            sectorBottom--;
	        }

	        topStage++;
	        if (topStage == 2) {
	            topStage = 0;
	            sectorTop--;
	        }

	    }
	    Log.d("calculateGridPoints", "mVisibleSectors @ 2 " + mVisibleSectors);
	    
	    gridRG.clearBuffers();
	    
	    //float[] a = { 0f,1000f, 1000f, 0f};
	    //gridRG.addVertices(a);

	    float minXGrid = (float) Math.floor(logicalOriginX / (mXGridSize * 2));
	    float maxXGrid = (float) Math.ceil((logicalOriginX + logicalWidth) / (mXGridSize * 2));
	    float minX = minXGrid * mXGridSize * 2;
	    float maxX = maxXGrid * mXGridSize * 2;
	    Log.d("calculateGridPoints","min_x " + minX + ", max_x " + maxX);

	    float minYGrid = (float) Math.floor(logicalOriginY / mYGridSize);
	    float minY = minYGrid * mYGridSize;
	    Log.d("calculateGridPoints","min_y " + minY);

	    // Draw vertical lines
	    float nextXPos = minX;
        while (nextXPos < logicalOriginX + logicalWidth) {
	        gridRG.addVertices(nextXPos, logicalOriginY, nextXPos, logicalOriginY + logicalHeight);
	        nextXPos += mXGridSize;
	    }

	    // drop/rise across the width between the x min & max grid lines
	    float ySlope = (maxXGrid - minXGrid) * mYGridSize;

	    // first loop; lines start at min y grid & move up until the line start is
	    // above the top of the graph
	    float yInc = 0;
	    float maxYGrid = minYGrid;
	    while (minY + yInc < logicalOriginY + logicalHeight) {

	        // right axes
	    	gridRG.addVertices(minX, minY + yInc, maxX, minY + yInc + ySlope);

	        // reverse x coords for mColumn axes
	    	gridRG.addVertices(maxX, minY + yInc, minX, minY + yInc + ySlope);

	        yInc += mYGridSize;
	        maxYGrid++;
	    }
	    
	    
	    //Log.d("calculateGridPoints","grid : x from minXGrid to maxXGrid, y from minYGrid to maxYGrid");

	    // second loop; lines start 1 grid below min y grid (we've already drawn the axes
	    // starting at min) and move down until the line end is below the bottom of the graph.
	    // right & mColumn axes done as above.
	    yInc = -mYGridSize;
	    while (minY + yInc + ySlope > logicalOriginY) {
	    	gridRG.addVertices(minX, minY + yInc, maxX, minY + yInc + ySlope);
	    	gridRG.addVertices(maxX, minY + yInc, minX, minY + yInc + ySlope);
	        yInc -= mYGridSize;
	    }

	    gridRG.finishBuffers();

	    return;

	}

	public void setFrame(IsoFrame isoFrame) {
		frame = isoFrame;
		sectorSize = isoFrame.getConfig().getInt("sector_size");
	}

}
